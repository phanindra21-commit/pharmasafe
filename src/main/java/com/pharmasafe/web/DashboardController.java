package com.pharmasafe.web;

import com.pharmasafe.service.DashboardService;
import com.pharmasafe.web.dto.Dtos.DashboardSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    /** GET /api/dashboard/summary → network-wide counts. */
    @GetMapping("/summary")
    public DashboardSummary summary() {
        return dashboard.summary();
    }
}
