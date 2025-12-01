package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String verDashboard(Model model) {
        model.addAttribute("stats", dashboardService.obtenerDatosDashboard());
        model.addAttribute("activePage", "dashboard");
        return "admin/dashboard";
    }
}