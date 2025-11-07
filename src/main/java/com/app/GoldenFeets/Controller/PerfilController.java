package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.DTO.PerfilInfoDTO;
import com.app.GoldenFeets.DTO.PerfilPasswordDTO;
import com.app.GoldenFeets.Entity.Usuario; // O tu entidad
import com.app.GoldenFeets.Service.UsuarioService; // O tu servicio de usuario
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService; // Inyecta tu servicio de usuario

    /**
     * Muestra la página de edición de perfil
     */
    @GetMapping
    public String mostrarPaginaPerfil(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Obtenemos el usuario actual
        Usuario usuario = usuarioService.buscarPorUsername(userDetails.getUsername());

        // --- CORRECCIÓN AQUÍ ---
        // Pre-llenamos los DTOs usando los campos en plural
        PerfilInfoDTO infoDTO = new PerfilInfoDTO();
        infoDTO.setNombres(usuario.getNombres());     // <-- Corregido a plural
        infoDTO.setApellidos(usuario.getApellidos()); // <-- Añadido
        infoDTO.setEmail(usuario.getEmail());
        infoDTO.setFotoUrl(usuario.getFotoUrl());

        model.addAttribute("usuario", usuario);
        model.addAttribute("infoDTO", infoDTO);
        model.addAttribute("passwordDTO", new PerfilPasswordDTO());
        model.addAttribute("activePage", "perfil");

        return "admin/perfil-form";
    }

    /**
     * Procesa la actualización de la información (Nombre, Email, Foto)
     */
    @PostMapping("/info")
    public String actualizarPerfilInfo(@ModelAttribute("infoDTO") PerfilInfoDTO infoDTO,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       RedirectAttributes redirectAttributes) {
        try {
            usuarioService.actualizarInfoPerfil(userDetails.getUsername(), infoDTO);
            redirectAttributes.addFlashAttribute("successInfo", "¡Perfil actualizado con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorInfo", "Error al actualizar el perfil: " + e.getMessage());
        }
        return "redirect:/admin/perfil";
    }

    /**
     * Procesa el cambio de contraseña
     */
    @PostMapping("/password")
    public String actualizarPerfilPassword(@ModelAttribute("passwordDTO") PerfilPasswordDTO passwordDTO,
                                           @AuthenticationPrincipal UserDetails userDetails,
                                           RedirectAttributes redirectAttributes) {
        try {
            usuarioService.cambiarPassword(userDetails.getUsername(), passwordDTO);
            redirectAttributes.addFlashAttribute("successPassword", "¡Contraseña cambiada con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorPassword", "Error al cambiar la contraseña: " + e.getMessage());
        }
        return "redirect:/admin/perfil";
    }
}