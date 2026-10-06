package com.pharmasafe.web;

import com.pharmasafe.service.SafetyService;
import com.pharmasafe.web.dto.Dtos.BatchVerificationResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final SafetyService safety;

    public BatchController(SafetyService safety) {
        this.safety = safety;
    }

    /** GET /api/batches/{batchNumber}/verify → batch details, safety alerts and overall risk. */
    @GetMapping("/{batchNumber}/verify")
    public BatchVerificationResponse verify(@PathVariable String batchNumber) {
        return safety.verify(batchNumber);
    }
}
