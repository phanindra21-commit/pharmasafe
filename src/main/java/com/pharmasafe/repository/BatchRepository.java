package com.pharmasafe.repository;

import com.pharmasafe.model.Batch;
import com.pharmasafe.model.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    Optional<Batch> findByBatchNumberIgnoreCase(String batchNumber);

    List<Batch> findByMedicineId(Long medicineId);

    long countByStatus(BatchStatus status);

    long countByExpiryDateBetween(LocalDate from, LocalDate to);
}
