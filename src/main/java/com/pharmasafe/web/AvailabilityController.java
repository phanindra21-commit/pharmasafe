package com.pharmasafe.web;

import com.pharmasafe.service.AvailabilityService;
import com.pharmasafe.web.dto.Dtos.AvailabilityResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availability;

    public AvailabilityController(AvailabilityService availability) {
        this.availability = availability;
    }

    /** GET /api/availability?medicine=amoxicillin&area=Tarnaka → stock across the network. */
    @GetMapping
    public AvailabilityResponse find(@RequestParam String medicine,
                                     @RequestParam(required = false) String area) {
        return availability.find(medicine, area);
    }
}
