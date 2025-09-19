package com.app.GoldenFeets.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Este método añade el objeto Authentication al modelo de TODAS las vistas.
     * Asegura que la información del usuario (si está logueado) esté siempre
     * disponible en cualquier plantilla de Thymeleaf, de forma automática.
     *
     * @param authentication El objeto de autenticación de Spring Security.
     * Será null si el usuario no ha iniciado sesión.
     * @return El objeto de autenticación.
     */
    @ModelAttribute("authentication")
    public Authentication getAuthentication(Authentication authentication) {
        return authentication;
    }
}