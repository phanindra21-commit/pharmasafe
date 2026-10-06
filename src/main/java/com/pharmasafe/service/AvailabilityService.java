package com.pharmasafe.service;

import com.pharmasafe.model.InventoryItem;
import com.pharmasafe.model.Pharmacy;
import com.pharmasafe.repository.InventoryItemRepository;
import com.pharmasafe.web.dto.Dtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

/**
 * Availability network: when one pharmacy is out of stock, find dispensable
 * stock (active, unexpired, not recalled) at participating pharmacies.
 */
@Service
@Transactional(readOnly = true)
public class AvailabilityService {

    private final InventoryItemRepository inventory;
    private final Clock clock;

    public AvailabilityService(InventoryItemRepository inventory, Clock clock) {
        this.inventory = inventory;
        this.clock = clock;
    }

    public AvailabilityResponse find(String medicineQuery, String preferredArea) {
        List<InventoryItem> items = inventory.findDispensableStock(medicineQuery.trim(), LocalDate.now(clock));

        // Group stock by pharmacy, keeping insertion order.
        Map<Long, List<InventoryItem>> byPharmacy = new LinkedHashMap<>();
        for (InventoryItem item : items) {
            byPharmacy.computeIfAbsent(item.getPharmacy().getId(), k -> new ArrayList<>()).add(item);
        }

        List<PharmacyStock> result = new ArrayList<>();
        for (List<InventoryItem> group : byPharmacy.values()) {
            Pharmacy p = group.get(0).getPharmacy();
            List<BatchStock> batchStocks = group.stream()
                    .map(i -> new BatchStock(i.getBatch().getBatchNumber(),
                            i.getBatch().getMedicine().getBrandName(),
                            i.getBatch().getExpiryDate(), i.getQuantity()))
                    .sorted(Comparator.comparing(BatchStock::expiryDate)) // earliest expiry first (FEFO)
                    .toList();
            int total = batchStocks.stream().mapToInt(BatchStock::quantity).sum();
            result.add(new PharmacyStock(p.getId(), p.getName(), p.getArea(), p.getPhone(), total, batchStocks));
        }

        // Pharmacies in the preferred area first, then the ones with most stock.
        String area = preferredArea == null ? "" : preferredArea.trim();
        result.sort(Comparator
                .comparing((PharmacyStock s) -> !area.isEmpty() && s.area() != null && s.area().equalsIgnoreCase(area) ? 0 : 1)
                .thenComparing(PharmacyStock::totalUnits, Comparator.reverseOrder()));

        int totalUnits = result.stream().mapToInt(PharmacyStock::totalUnits).sum();
        String note = result.isEmpty()
                ? "No dispensable stock found in the network. Recalled and expired batches are excluded."
                : "Only active, unexpired, non-recalled batches are shown. Call the pharmacy to confirm before sending a patient.";
        return new AvailabilityResponse(medicineQuery, area.isEmpty() ? null : area, totalUnits, result, note);
    }
}
