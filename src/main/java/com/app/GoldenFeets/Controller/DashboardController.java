package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Pedido;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Añadimos un atributo para saber qué página está activa en el sidebar
        model.addAttribute("activePage", "dashboard");
        return "admin/dashboard"; // Apunta a templates/admin/dashboard.html
    }
}