package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Usuario;
import com.app.GoldenFeets.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UsuarioRepository usuarioRepository;

    @ModelAttribute("globalUsuario")
    public Usuario getGlobalUsuario() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 1. Validaciones básicas de sesión
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            Object principal = authentication.getPrincipal();

            // Si es usuario anónimo (no logueado), retornamos null
            if (principal.equals("anonymousUser")) {
                return null;
            }

            String username = "";
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }

            // 2. Consulta a BD protegida
            // Si esto falla (por ejemplo, falta la columna foto_url),
            // saltará al catch y la página cargará igual.
            return usuarioRepository.findByEmail(username).orElse(null);

        } catch (Exception e) {
            // ESTO ES LO IMPORTANTE:
            // Si hay un error (ej: columna no existe), lo imprimimos en consola
            // pero NO rompemos la página web.
            System.err.println("--- ERROR EN GLOBAL ADVICE (Perfil Header) ---");
            System.err.println("Causa: " + e.getMessage());
            e.printStackTrace();
            return null; // Retornamos null para que el HTML simplemente muestre la imagen por defecto
        }
    }
}