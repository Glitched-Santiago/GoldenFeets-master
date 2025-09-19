package com.app.GoldenFeets.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String landingPage() {
        // Ya no necesitamos pasar la autenticación. El ControllerAdvice lo hace por nosotros.
        return "landing";
    }
}