package com.pharmasafe.service;

import com.pharmasafe.model.*;
import com.pharmasafe.repository.BatchRepository;
import com.pharmasafe.repository.MedicineRepository;
import com.pharmasafe.repository.RecallRepository;
import com.pharmasafe.web.dto.Dtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Safety engine: looks at one batch and collects every safety signal
 * (recall, quarantine, expiry, look-alike names) into a single risk level.
 *
 * The final dispensing decision always stays with the pharmacist.
 */
@Service
@Transactional(readOnly = true)
public class SafetyService {

    /** Batches expiring within this many days need a closer look. */
    static final int EXPIRY_REVIEW_DAYS = 30;
    /** Batches expiring within this many days get an informational note. */
    static final int EXPIRY_NOTICE_DAYS = 90;

    private final BatchRepository batches;
    private final MedicineRepository medicines;
    private final RecallRepository recalls;
    private final Clock clock;

    public SafetyService(BatchRepository batches, MedicineRepository medicines,
                         RecallRepository recalls, Clock clock) {
        this.batches = batches;
        this.medicines = medicines;
        this.recalls = recalls;
        this.clock = clock;
    }

    public BatchVerificationResponse verify(String batchNumber) {
        Batch batch = batches.findByBatchNumberIgnoreCase(batchNumber.trim())
                .orElseThrow(() -> new NotFoundException(
                        "No batch '" + batchNumber + "' found. Check the number printed on the strip or carton."));

        List<Recall> activeRecalls = recalls.findByBatchIdAndActiveTrue(batch.getId());
        List<Medicine> lookAlikes = batch.getMedicine().getLookAlikeGroup() == null
                ? List.of()
                : medicines.findByLookAlikeGroupAndIdNot(batch.getMedicine().getLookAlikeGroup(),
                                                         batch.getMedicine().getId());

        List<SafetyAlert> alerts = evaluate(batch, activeRecalls, lookAlikes, LocalDate.now(clock));
        RiskLevel risk = alerts.stream().map(SafetyAlert::level)
                .reduce(RiskLevel.LOW, RiskLevel::max);

        return new BatchVerificationResponse(
                batch.getBatchNumber(),
                MedicineDto.from(batch.getMedicine()),
                batch.getManufactureDate(),
                batch.getExpiryDate(),
                batch.getSupplier(),
                batch.getStatus(),
                risk,
                alerts,
                activeRecalls.stream().map(RecallDto::from).toList(),
                "PharmaSafe assists verification. The final decision remains with the pharmacist.");
    }

    /**
     * Pure rule evaluation, kept separate from the database so it is easy to unit-test.
     */
    static List<SafetyAlert> evaluate(Batch batch, List<Recall> activeRecalls,
                                      List<Medicine> lookAlikes, LocalDate today) {
        List<SafetyAlert> alerts = new ArrayList<>();

        for (Recall r : activeRecalls) {
            alerts.add(new SafetyAlert("RECALL", RiskLevel.HIGH_PRIORITY,
                    "Batch under " + r.getSeverity() + " recall: " + r.getReason()
                            + " (issued " + r.getIssuedOn() + "). Do not dispense; quarantine stock."));
        }
        if (batch.getStatus() == BatchStatus.QUARANTINED) {
            alerts.add(new SafetyAlert("QUARANTINE", RiskLevel.HIGH_PRIORITY,
                    "Batch is quarantined pending investigation. Do not dispense."));
        }

        long daysToExpiry = ChronoUnit.DAYS.between(today, batch.getExpiryDate());
        if (daysToExpiry < 0) {
            alerts.add(new SafetyAlert("EXPIRED", RiskLevel.HIGH_PRIORITY,
                    "Expired " + (-daysToExpiry) + " day(s) ago on " + batch.getExpiryDate() + ". Do not dispense."));
        } else if (daysToExpiry <= EXPIRY_REVIEW_DAYS) {
            alerts.add(new SafetyAlert("EXPIRING_SOON", RiskLevel.REVIEW_REQUIRED,
                    "Expires in " + daysToExpiry + " day(s) on " + batch.getExpiryDate()
                            + ". Check the patient's course length before dispensing."));
        } else if (daysToExpiry <= EXPIRY_NOTICE_DAYS) {
            alerts.add(new SafetyAlert("EXPIRY_NOTICE", RiskLevel.LOW,
                    "Expires in " + daysToExpiry + " days on " + batch.getExpiryDate() + "."));
        }

        if (!lookAlikes.isEmpty()) {
            String names = lookAlikes.stream()
                    .map(m -> m.getBrandName() + " (" + m.getGenericName() + ")")
                    .collect(Collectors.joining(", "));
            alerts.add(new SafetyAlert("LOOK_ALIKE_NAME", RiskLevel.REVIEW_REQUIRED,
                    "Name can be confused with: " + names + ". Confirm the prescription before dispensing."));
        }
        return alerts;
    }
}
