package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @GetMapping("/registro")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("usuario", new Cliente());
        // --- CAMBIO AQUÍ ---
        return "login/registro"; // Apunta a la vista login/registro.html
    }

    @PostMapping("/registro")
    public String procesarRegistro(Cliente usuario, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.saveOrUpdate(usuario, "CLIENTE");
            redirectAttributes.addFlashAttribute("successMessage", "¡Registro exitoso! Por favor, inicia sesión.");
            // Esto no cambia, ya que es una redirección de URL
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "El email ya está en uso. Por favor, intenta con otro.");
            // Esto no cambia, ya que es una redirección de URL
            return "redirect:/registro";
        }
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        // --- CAMBIO AQUÍ ---
        return "login/login"; // Apunta a la vista login/login.html
    }
}