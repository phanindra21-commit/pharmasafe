package com.pharmasafe.repository;

import com.pharmasafe.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    /** Every pharmacy holding stock of this batch (used for recall impact). */
    List<InventoryItem> findByBatchIdAndQuantityGreaterThan(Long batchId, int minQuantity);

    /**
     * Dispensable stock of a medicine across the network:
     * matching name, batch ACTIVE, not expired, quantity > 0.
     */
    @Query("""
           select i from InventoryItem i
           where (lower(i.batch.medicine.brandName) like lower(concat('%', :q, '%'))
               or lower(i.batch.medicine.genericName) like lower(concat('%', :q, '%')))
             and i.batch.status = com.pharmasafe.model.BatchStatus.ACTIVE
             and i.batch.expiryDate > :today
             and i.quantity > 0
           order by i.pharmacy.name
           """)
    List<InventoryItem> findDispensableStock(@Param("q") String medicineQuery,
                                             @Param("today") LocalDate today);

    /** Total units sitting in recalled batches across the network. */
    @Query("""
           select coalesce(sum(i.quantity), 0) from InventoryItem i
           where i.batch.status = com.pharmasafe.model.BatchStatus.RECALLED
           """)
    long totalUnitsInRecalledBatches();
}
