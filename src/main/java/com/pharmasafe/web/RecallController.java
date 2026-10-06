package com.pharmasafe.web;

import com.pharmasafe.service.RecallService;
import com.pharmasafe.web.dto.Dtos.RecallDto;
import com.pharmasafe.web.dto.Dtos.RecallImpactResponse;
import com.pharmasafe.web.dto.Dtos.RecallRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recalls")
public class RecallController {

    private final RecallService recalls;

    public RecallController(RecallService recalls) {
        this.recalls = recalls;
    }

    /** GET /api/recalls → all active recalls, newest first. */
    @GetMapping
    public List<RecallDto> list() {
        return recalls.listActive();
    }

    /** POST /api/recalls → register a recall; returns the impact straight away. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecallImpactResponse create(@Valid @RequestBody RecallRequest request) {
        return recalls.create(request);
    }

    /** GET /api/recalls/{id}/impact → which pharmacies hold the recalled batch, and how many units. */
    @GetMapping("/{id}/impact")
    public RecallImpactResponse impact(@PathVariable Long id) {
        return recalls.impact(id);
    }
}
