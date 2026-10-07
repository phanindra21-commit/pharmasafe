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

    /**
     * GET /api/availability?medicine=amoxicillin&area=Tarnaka&radiusKm=5
     * → safe stock in pharmacies within radiusKm of the area, nearest first.
     */
    @GetMapping
    public AvailabilityResponse find(@RequestParam String medicine,
                                     @RequestParam(required = false) String area,
                                     @RequestParam(required = false) Double radiusKm) {
        return availability.find(medicine, area, radiusKm);
    }
}
