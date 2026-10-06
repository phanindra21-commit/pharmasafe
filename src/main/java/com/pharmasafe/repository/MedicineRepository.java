package com.pharmasafe.repository;

import com.pharmasafe.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    /** Case-insensitive search on brand or generic name. */
    @Query("""
           select m from Medicine m
           where lower(m.brandName) like lower(concat('%', :q, '%'))
              or lower(m.genericName) like lower(concat('%', :q, '%'))
           order by m.brandName
           """)
    List<Medicine> search(@Param("q") String query);

    /** Other medicines in the same look-alike/sound-alike group. */
    List<Medicine> findByLookAlikeGroupAndIdNot(String lookAlikeGroup, Long id);
}
