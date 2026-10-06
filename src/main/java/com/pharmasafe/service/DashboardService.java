package com.pharmasafe.service;

import com.pharmasafe.repository.*;
import com.pharmasafe.web.dto.Dtos.DashboardSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/** Network-wide numbers for the dashboard. */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final MedicineRepository medicines;
    private final BatchRepository batches;
    private final PharmacyRepository pharmacies;
    private final RecallRepository recalls;
    private final InventoryItemRepository inventory;
    private final Clock clock;

    public DashboardService(MedicineRepository medicines, BatchRepository batches,
                            PharmacyRepository pharmacies, RecallRepository recalls,
                            InventoryItemRepository inventory, Clock clock) {
        this.medicines = medicines;
        this.batches = batches;
        this.pharmacies = pharmacies;
        this.recalls = recalls;
        this.inventory = inventory;
        this.clock = clock;
    }

    public DashboardSummary summary() {
        LocalDate today = LocalDate.now(clock);
        return new DashboardSummary(
                medicines.count(),
                batches.count(),
                pharmacies.count(),
                recalls.countByActiveTrue(),
                inventory.totalUnitsInRecalledBatches(),
                batches.countByExpiryDateBetween(today, today.plusDays(SafetyService.EXPIRY_REVIEW_DAYS)));
    }
}
