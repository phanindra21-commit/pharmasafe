package com.pharmasafe.service;

import com.pharmasafe.model.*;
import com.pharmasafe.web.dto.Dtos.SafetyAlert;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for the safety rules. No database or Spring needed. */
class SafetyServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 7);
    private final Medicine paracil = new Medicine("Paracil 500", "Paracetamol", "500 mg", "Tablet", "Mediora Labs", null);

    private Batch batchExpiringIn(int days) {
        return new Batch("TEST-1", paracil, TODAY.minusMonths(6), TODAY.plusDays(days), "Test Supplier");
    }

    private static List<String> types(List<SafetyAlert> alerts) {
        return alerts.stream().map(SafetyAlert::type).toList();
    }

    @Test
    void healthyBatchHasNoAlerts() {
        List<SafetyAlert> alerts = SafetyService.evaluate(batchExpiringIn(400), List.of(), List.of(), TODAY);
        assertThat(alerts).isEmpty();
    }

    @Test
    void expiredBatchIsHighPriority() {
        List<SafetyAlert> alerts = SafetyService.evaluate(batchExpiringIn(-1), List.of(), List.of(), TODAY);
        assertThat(types(alerts)).containsExactly("EXPIRED");
        assertThat(alerts.get(0).level()).isEqualTo(RiskLevel.HIGH_PRIORITY);
    }

    @Test
    void batchExpiringOnTheReviewBoundaryNeedsReview() {
        List<SafetyAlert> alerts = SafetyService.evaluate(
                batchExpiringIn(SafetyService.EXPIRY_REVIEW_DAYS), List.of(), List.of(), TODAY);
        assertThat(types(alerts)).containsExactly("EXPIRING_SOON");
        assertThat(alerts.get(0).level()).isEqualTo(RiskLevel.REVIEW_REQUIRED);
    }

    @Test
    void batchExpiringWithinNinetyDaysGetsLowNotice() {
        List<SafetyAlert> alerts = SafetyService.evaluate(batchExpiringIn(60), List.of(), List.of(), TODAY);
        assertThat(types(alerts)).containsExactly("EXPIRY_NOTICE");
        assertThat(alerts.get(0).level()).isEqualTo(RiskLevel.LOW);
    }

    @Test
    void activeRecallIsHighPriority() {
        Batch batch = batchExpiringIn(400);
        Recall recall = new Recall(batch, "Contamination", RecallSeverity.CLASS_I, "Regulator", TODAY);
        List<SafetyAlert> alerts = SafetyService.evaluate(batch, List.of(recall), List.of(), TODAY);
        assertThat(types(alerts)).containsExactly("RECALL");
        assertThat(alerts.get(0).level()).isEqualTo(RiskLevel.HIGH_PRIORITY);
    }

    @Test
    void quarantinedBatchIsHighPriority() {
        Batch batch = batchExpiringIn(400);
        batch.setStatus(BatchStatus.QUARANTINED);
        assertThat(types(SafetyService.evaluate(batch, List.of(), List.of(), TODAY))).containsExactly("QUARANTINE");
    }

    @Test
    void lookAlikeNamesNeedReview() {
        Medicine metronal = new Medicine("Metronal 400", "Metronidazole", "400 mg", "Tablet", "X", "G1");
        List<SafetyAlert> alerts = SafetyService.evaluate(batchExpiringIn(400), List.of(), List.of(metronal), TODAY);
        assertThat(types(alerts)).containsExactly("LOOK_ALIKE_NAME");
        assertThat(alerts.get(0).message()).contains("Metronal 400");
    }

    @Test
    void severalProblemsAreAllReported() {
        Batch batch = batchExpiringIn(10);
        Recall recall = new Recall(batch, "Mislabelled", RecallSeverity.CLASS_II, "Regulator", TODAY);
        List<SafetyAlert> alerts = SafetyService.evaluate(batch, List.of(recall), List.of(), TODAY);
        assertThat(types(alerts)).containsExactlyInAnyOrder("RECALL", "EXPIRING_SOON");
    }

    @Test
    void riskLevelMaxPicksTheMoreSerious() {
        assertThat(RiskLevel.LOW.max(RiskLevel.REVIEW_REQUIRED)).isEqualTo(RiskLevel.REVIEW_REQUIRED);
        assertThat(RiskLevel.HIGH_PRIORITY.max(RiskLevel.LOW)).isEqualTo(RiskLevel.HIGH_PRIORITY);
    }
}
