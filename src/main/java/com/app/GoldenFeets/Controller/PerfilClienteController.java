package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.DTO.PerfilInfoDTO;
import com.app.GoldenFeets.DTO.PerfilPasswordDTO;
import com.app.GoldenFeets.Entity.Usuario;
import com.app.GoldenFeets.Config.CustomUserDetails;
import com.app.GoldenFeets.Service.UploadFileService;
import com.app.GoldenFeets.Service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
@RequiredArgsConstructor
public class PerfilClienteController {

    private final UsuarioService usuarioService;
    private final UploadFileService uploadService; // Tu servicio de carga

    @GetMapping
    public String verMiPerfil(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        Usuario usuario = usuarioService.buscarPorUsername(userDetails.getUsername());

        PerfilInfoDTO infoDTO = new PerfilInfoDTO();
        infoDTO.setNombres(usuario.getNombres());
        infoDTO.setApellidos(usuario.getApellidos());
        infoDTO.setEmail(usuario.getEmail());
        infoDTO.setFotoUrl(usuario.getFotoUrl());

        model.addAttribute("usuario", usuario);
        model.addAttribute("infoDTO", infoDTO);
        model.addAttribute("passwordDTO", new PerfilPasswordDTO());

        return "cliente/perfil";
    }

    @PostMapping("/info")
    public String actualizarInfo(@ModelAttribute("infoDTO") PerfilInfoDTO infoDTO,
                                 @RequestParam(value = "file", required = false) MultipartFile file, // Recibimos el archivo
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            // --- DEBUG: Ver en consola si llega el archivo ---
            if (file != null && !file.isEmpty()) {
                System.out.println("Imagen recibida: " + file.getOriginalFilename());
            } else {
                System.out.println("No se recibió ninguna imagen nueva.");
            }
            // -------------------------------------------------

            Usuario usuarioActual = userDetails.getUsuario();

            // 1. Si hay archivo nuevo, guardarlo
            if (file != null && !file.isEmpty()) {
                String nombreImagen = uploadService.saveImage(file);
                infoDTO.setFotoUrl("/images/" + nombreImagen); // Guardamos la ruta relativa
            } else {
                // Si no, mantenemos la anterior
                infoDTO.setFotoUrl(usuarioActual.getFotoUrl());
            }

            // 2. Actualizar BD
            usuarioService.actualizarInfoPerfil(userDetails.getUsername(), infoDTO);

            // 3. Actualizar Sesión (para ver el cambio inmediato en el header)
            usuarioActual.setFotoUrl(infoDTO.getFotoUrl());
            usuarioActual.setNombres(infoDTO.getNombres());

            redirectAttributes.addFlashAttribute("successInfo", "Perfil actualizado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorInfo", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/perfil";
    }

    @PostMapping("/password")
    public String cambiarPassword(@ModelAttribute("passwordDTO") PerfilPasswordDTO passwordDTO,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            usuarioService.cambiarPassword(userDetails.getUsername(), passwordDTO);
            redirectAttributes.addFlashAttribute("successPassword", "Contraseña actualizada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorPassword", "Error: " + e.getMessage());
        }
        return "redirect:/perfil";
    }
}