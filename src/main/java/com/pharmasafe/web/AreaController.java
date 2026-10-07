package com.pharmasafe.web;

import com.pharmasafe.service.AreaDirectory;
import com.pharmasafe.service.AreaDirectory.Area;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/areas")
public class AreaController {

    private final AreaDirectory areas;

    public AreaController(AreaDirectory areas) {
        this.areas = areas;
    }

    /** GET /api/areas → the localities a pharmacist can search from. */
    @GetMapping
    public List<Area> list() {
        return areas.all();
    }
}
