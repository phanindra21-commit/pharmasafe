package com.pharmasafe.repository;

import com.pharmasafe.model.Recall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecallRepository extends JpaRepository<Recall, Long> {

    List<Recall> findByBatchIdAndActiveTrue(Long batchId);

    List<Recall> findByActiveTrueOrderByIssuedOnDesc();

    long countByActiveTrue();
}
