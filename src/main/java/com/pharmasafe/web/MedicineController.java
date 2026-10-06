package com.pharmasafe.web;

import com.pharmasafe.repository.BatchRepository;
import com.pharmasafe.repository.MedicineRepository;
import com.pharmasafe.service.NotFoundException;
import com.pharmasafe.web.dto.Dtos.BatchSummaryDto;
import com.pharmasafe.web.dto.Dtos.MedicineDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineRepository medicines;
    private final BatchRepository batches;

    public MedicineController(MedicineRepository medicines, BatchRepository batches) {
        this.medicines = medicines;
        this.batches = batches;
    }

    /** GET /api/medicines?query=para  → search by brand or generic name. */
    @GetMapping
    public List<MedicineDto> search(@RequestParam(defaultValue = "") String query) {
        return medicines.search(query.trim()).stream().map(MedicineDto::from).toList();
    }

    /** GET /api/medicines/{id}/batches → all batches of one medicine. */
    @GetMapping("/{id}/batches")
    public List<BatchSummaryDto> batches(@PathVariable Long id) {
        if (!medicines.existsById(id)) {
            throw new NotFoundException("No medicine with id " + id + ".");
        }
        return batches.findByMedicineId(id).stream().map(BatchSummaryDto::from).toList();
    }
}
