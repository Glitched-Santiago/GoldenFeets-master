package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/correos")
@RequiredArgsConstructor
public class CorreoAdminController {

    private final EmailService emailService;

    // Muestra el formulario para redactar
    @GetMapping("/redactar")
    public String mostrarFormularioRedaccion() {
        return "admin/redactar-correo";
    }

    // Procesa el envío
    @PostMapping("/enviar")
    public String procesarEnvio(
            @RequestParam("tipo") String tipo,
            @RequestParam("asunto") String asunto,
            @RequestParam("mensaje") String mensaje,
            RedirectAttributes redirectAttributes) {

        // Llamamos al servicio asíncrono
        emailService.enviarCorreoMasivo(tipo, asunto, mensaje);

        redirectAttributes.addFlashAttribute("successMessage",
                "El proceso de envío ha comenzado en segundo plano.");

        return "redirect:/admin/usuarios";
    }
}