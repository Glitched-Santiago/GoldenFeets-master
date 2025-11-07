package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Usuario;
import com.app.GoldenFeets.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor; // <-- Asegúrate de importar esto
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor // <-- 1. Añade esta anotación
public class GlobalControllerAdvice {

    // 2. Haz el repositorio 'final' y quita @Autowired
    private final UsuarioRepository usuarioRepository;

    @ModelAttribute("globalUsuario")
    public Usuario getGlobalUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 3. Comprobación de seguridad robusta
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null; // Si no está logueado, devuelve null
        }

        String username = "";
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        // 4. Busca en tu repositorio y devuelve null si no lo encuentra
        //    (Tu base-admin.html ya maneja el caso de que sea null)
        return usuarioRepository.findByEmail(username)
                .orElse(null);
    }
}