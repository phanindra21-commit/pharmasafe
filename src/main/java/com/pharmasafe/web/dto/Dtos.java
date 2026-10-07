package com.pharmasafe.web.dto;

import com.pharmasafe.model.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Response/request shapes for the REST API.
 * Records keep the API separate from the database entities.
 */
public final class Dtos {

    private Dtos() { }

    public record MedicineDto(Long id, String brandName, String genericName,
                              String strength, String dosageForm, String manufacturer) {
        public static MedicineDto from(Medicine m) {
            return new MedicineDto(m.getId(), m.getBrandName(), m.getGenericName(),
                    m.getStrength(), m.getDosageForm(), m.getManufacturer());
        }
    }

    public record BatchSummaryDto(String batchNumber, LocalDate expiryDate, BatchStatus status, String supplier) {
        public static BatchSummaryDto from(Batch b) {
            return new BatchSummaryDto(b.getBatchNumber(), b.getExpiryDate(), b.getStatus(), b.getSupplier());
        }
    }

    /** One safety signal found for a batch. */
    public record SafetyAlert(String type, RiskLevel level, String message) { }

    public record RecallDto(Long id, String batchNumber, String brandName, String reason,
                            RecallSeverity severity, String issuedBy, LocalDate issuedOn) {
        public static RecallDto from(Recall r) {
            return new RecallDto(r.getId(), r.getBatch().getBatchNumber(),
                    r.getBatch().getMedicine().getBrandName(), r.getReason(),
                    r.getSeverity(), r.getIssuedBy(), r.getIssuedOn());
        }
    }

    public record BatchVerificationResponse(String batchNumber, MedicineDto medicine,
                                            LocalDate manufactureDate, LocalDate expiryDate,
                                            String supplier, BatchStatus status,
                                            RiskLevel riskLevel, List<SafetyAlert> alerts,
                                            List<RecallDto> activeRecalls, String note) { }

    public record BatchStock(String batchNumber, String brandName, LocalDate expiryDate, int quantity) { }

    public record PharmacyStock(Long pharmacyId, String pharmacyName, String area, String phone,
                                Double latitude, Double longitude, Double distanceKm,
                                int totalUnits, List<BatchStock> batches) { }

    public record AvailabilityResponse(String medicineQuery, String preferredArea,
                                       Double centerLatitude, Double centerLongitude, Double radiusKm,
                                       int totalUnits, List<PharmacyStock> pharmacies, String note) { }

    public record RecallRequest(@NotBlank String batchNumber,
                                @NotBlank String reason,
                                @NotNull RecallSeverity severity,
                                String issuedBy) { }

    public record AffectedPharmacy(String pharmacyName, String area, String phone, int quantity) { }

    public record RecallImpactResponse(RecallDto recall, int affectedPharmacies, int totalUnits,
                                       List<AffectedPharmacy> pharmacies, List<String> recommendedActions) { }

    public record DashboardSummary(long medicines, long batches, long pharmacies,
                                   long activeRecalls, long unitsInRecalledBatches,
                                   long batchesExpiringIn30Days) { }

    public record ApiError(int status, String error, String message) { }
}
