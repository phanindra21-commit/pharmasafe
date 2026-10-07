package com.pharmasafe.service;

import com.pharmasafe.model.InventoryItem;
import com.pharmasafe.model.Pharmacy;
import com.pharmasafe.repository.InventoryItemRepository;
import com.pharmasafe.service.AreaDirectory.Area;
import com.pharmasafe.web.dto.Dtos.AvailabilityResponse;
import com.pharmasafe.web.dto.Dtos.BatchStock;
import com.pharmasafe.web.dto.Dtos.PharmacyStock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Inter-pharmacy availability: where can a patient get a SAFE batch of this medicine nearby?
 *
 * Only ACTIVE, unexpired batches with stock are considered. When an area is given, pharmacies
 * are measured from that area's centre, filtered to the search radius and sorted nearest first.
 * Within each pharmacy, batches are listed earliest expiry first (FEFO).
 */
@Service
@Transactional(readOnly = true)
public class AvailabilityService {

    /** Radius used when the caller does not give one. */
    static final double DEFAULT_RADIUS_KM = 5.0;
    /** How many pharmacies to show when nothing is inside the radius. */
    static final int FALLBACK_COUNT = 3;

    private final InventoryItemRepository inventory;
    private final AreaDirectory areas;
    private final Clock clock;

    public AvailabilityService(InventoryItemRepository inventory, AreaDirectory areas, Clock clock) {
        this.inventory = inventory;
        this.areas = areas;
        this.clock = clock;
    }

    public AvailabilityResponse find(String medicineQuery, String areaName, Double radiusKm) {
        String query = medicineQuery == null ? "" : medicineQuery.trim();
        List<InventoryItem> items = inventory.findDispensableStock(query, LocalDate.now(clock));
        Optional<Area> area = areas.find(areaName);
        double radius = (radiusKm == null || radiusKm <= 0) ? DEFAULT_RADIUS_KM : radiusKm;

        // Group stock by pharmacy, keeping insertion order.
        Map<Long, List<InventoryItem>> byPharmacy = new LinkedHashMap<>();
        for (InventoryItem item : items) {
            byPharmacy.computeIfAbsent(item.getPharmacy().getId(), k -> new ArrayList<>()).add(item);
        }

        List<PharmacyStock> all = new ArrayList<>();
        for (List<InventoryItem> group : byPharmacy.values()) {
            Pharmacy p = group.get(0).getPharmacy();
            List<BatchStock> batchStocks = group.stream()
                    .map(i -> new BatchStock(i.getBatch().getBatchNumber(),
                            i.getBatch().getMedicine().getBrandName(),
                            i.getBatch().getExpiryDate(), i.getQuantity()))
                    .sorted(Comparator.comparing(BatchStock::expiryDate)) // earliest expiry first (FEFO)
                    .toList();
            int total = batchStocks.stream().mapToInt(BatchStock::quantity).sum();
            Double distance = area.isPresent() ? distanceFrom(area.get(), p) : null;
            all.add(new PharmacyStock(p.getId(), p.getName(), p.getArea(), p.getPhone(),
                    p.getLatitude(), p.getLongitude(), distance, total, batchStocks));
        }

        List<PharmacyStock> result;
        String note;
        if (area.isEmpty()) {
            // No (known) area: whole network, most stock first.
            all.sort(Comparator.comparing(PharmacyStock::totalUnits, Comparator.reverseOrder()));
            result = all;
            note = all.isEmpty()
                    ? "No dispensable stock found in the network. Recalled, quarantined and expired batches are excluded."
                    : "Showing the whole network. Choose an area to see the nearest pharmacies first.";
        } else {
            all.sort(Comparator.comparing(PharmacyStock::distanceKm,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            List<PharmacyStock> inside = all.stream()
                    .filter(s -> s.distanceKm() != null && s.distanceKm() <= radius)
                    .toList();
            if (!inside.isEmpty()) {
                result = inside;
                note = "Pharmacies within " + format(radius) + " km of " + area.get().name()
                        + ", nearest first. Only active, unexpired, non-recalled batches are shown.";
            } else if (!all.isEmpty()) {
                result = all.subList(0, Math.min(FALLBACK_COUNT, all.size()));
                note = "No stock within " + format(radius) + " km of " + area.get().name()
                        + ". Showing the nearest pharmacies that have it.";
            } else {
                result = List.of();
                note = "No dispensable stock found in the network. Recalled, quarantined and expired batches are excluded.";
            }
        }

        int totalUnits = result.stream().mapToInt(PharmacyStock::totalUnits).sum();
        return new AvailabilityResponse(query,
                area.map(Area::name).orElse(null),
                area.map(Area::latitude).orElse(null),
                area.map(Area::longitude).orElse(null),
                area.isPresent() ? radius : null,
                totalUnits, List.copyOf(result), note);
    }

    private static Double distanceFrom(Area area, Pharmacy p) {
        if (p.getLatitude() == null || p.getLongitude() == null) {
            return null;
        }
        double km = AreaDirectory.distanceKm(area.latitude(), area.longitude(), p.getLatitude(), p.getLongitude());
        return Math.round(km * 10.0) / 10.0;   // one decimal place
    }

    private static String format(double km) {
        return km == Math.floor(km) ? String.valueOf((long) km) : String.valueOf(km);
    }
}
