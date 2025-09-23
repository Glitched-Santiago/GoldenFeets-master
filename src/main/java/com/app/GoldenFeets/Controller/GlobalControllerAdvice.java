package com.app.GoldenFeets.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("authentication")
    public Authentication getAuthentication() {
        // Esto hace que el objeto "authentication" esté disponible en todas las plantillas
        return SecurityContextHolder.getContext().getAuthentication();
    }
}