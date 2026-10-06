package com.pharmasafe.service;

import com.pharmasafe.model.Batch;
import com.pharmasafe.model.BatchStatus;
import com.pharmasafe.model.InventoryItem;
import com.pharmasafe.model.Recall;
import com.pharmasafe.repository.BatchRepository;
import com.pharmasafe.repository.InventoryItemRepository;
import com.pharmasafe.repository.RecallRepository;
import com.pharmasafe.web.dto.Dtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Recall response: register a recall against a batch and trace it
 * Recall -> Batch -> Pharmacy -> Inventory to see the impact.
 */
@Service
public class RecallService {

    private final RecallRepository recalls;
    private final BatchRepository batches;
    private final InventoryItemRepository inventory;
    private final Clock clock;

    public RecallService(RecallRepository recalls, BatchRepository batches,
                         InventoryItemRepository inventory, Clock clock) {
        this.recalls = recalls;
        this.batches = batches;
        this.inventory = inventory;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<RecallDto> listActive() {
        return recalls.findByActiveTrueOrderByIssuedOnDesc().stream().map(RecallDto::from).toList();
    }

    @Transactional
    public RecallImpactResponse create(RecallRequest request) {
        Batch batch = batches.findByBatchNumberIgnoreCase(request.batchNumber().trim())
                .orElseThrow(() -> new NotFoundException("No batch '" + request.batchNumber() + "' found."));

        if (!recalls.findByBatchIdAndActiveTrue(batch.getId()).isEmpty()) {
            throw new ConflictException("Batch " + batch.getBatchNumber() + " already has an active recall.");
        }

        String issuer = (request.issuedBy() == null || request.issuedBy().isBlank())
                ? "Manual entry" : request.issuedBy().trim();
        Recall recall = recalls.save(new Recall(batch, request.reason().trim(), request.severity(),
                issuer, LocalDate.now(clock)));
        batch.setStatus(BatchStatus.RECALLED);
        batches.save(batch);

        return buildImpact(recall);
    }

    @Transactional(readOnly = true)
    public RecallImpactResponse impact(Long recallId) {
        Recall recall = recalls.findById(recallId)
                .orElseThrow(() -> new NotFoundException("No recall with id " + recallId + "."));
        return buildImpact(recall);
    }

    private RecallImpactResponse buildImpact(Recall recall) {
        List<InventoryItem> held = inventory.findByBatchIdAndQuantityGreaterThan(recall.getBatch().getId(), 0);

        List<AffectedPharmacy> pharmacies = held.stream()
                .map(i -> new AffectedPharmacy(i.getPharmacy().getName(), i.getPharmacy().getArea(),
                        i.getPharmacy().getPhone(), i.getQuantity()))
                .sorted(Comparator.comparing(AffectedPharmacy::quantity).reversed())
                .toList();
        int totalUnits = pharmacies.stream().mapToInt(AffectedPharmacy::quantity).sum();

        List<String> actions = List.of(
                "Notify the " + pharmacies.size() + " affected pharmacies to stop dispensing batch "
                        + recall.getBatch().getBatchNumber() + ".",
                "Quarantine " + totalUnits + " unit(s) and return them to the supplier ("
                        + recall.getBatch().getSupplier() + ").",
                "Check recent sales of this batch so patients can be contacted if required.",
                "Use the availability search to find a safe alternative batch for affected patients.");

        return new RecallImpactResponse(RecallDto.from(recall), pharmacies.size(), totalUnits, pharmacies, actions);
    }
}
