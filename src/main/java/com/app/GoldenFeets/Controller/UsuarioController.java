package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Administrador;
import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.Usuario;
import com.app.GoldenFeets.Service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ... (método listarUsuarios no cambia) ...
    @GetMapping
    public String listarUsuarios(@RequestParam(required = false) String id,
                                 @RequestParam(required = false) String nombres,
                                 @RequestParam(required = false) String apellidos,
                                 @RequestParam(required = false) String numeroDocumento,
                                 @RequestParam(required = false) String email,
                                 @RequestParam(required = false) String rol,
                                 Model model) {
        model.addAttribute("usuarios", usuarioService.search(id, nombres, apellidos, numeroDocumento, email, rol));
        model.addAttribute("idParam", id);
        model.addAttribute("nombresParam", nombres);
        model.addAttribute("apellidosParam", apellidos);
        model.addAttribute("numeroDocumentoParam", numeroDocumento);
        model.addAttribute("emailParam", email);
        model.addAttribute("rolParam", rol);
        model.addAttribute("activePage", "usuarios");
        return "usuarios/usuarios";
    }

    // Muestra el formulario para CREAR un nuevo usuario
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        // Le pasamos un objeto Cliente concreto para que el formulario sepa qué campos esperar
        model.addAttribute("usuario", new Cliente());
        model.addAttribute("isEditMode", false);
        model.addAttribute("activePage", "usuarios");
        return "usuarios/usuarios-form";
    }

    // Muestra el formulario para EDITAR un usuario existente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de Usuario no válido: " + id));
        usuario.setPassword(null); // Borramos la contraseña para no mostrarla
        model.addAttribute("usuario", usuario);
        model.addAttribute("isEditMode", true);
        model.addAttribute("activePage", "usuarios");
        return "usuarios/usuarios-form";
    }

    /**
     * MÉTODO CORREGIDO: Guarda un usuario nuevo o actualiza uno existente.
     * Ya no usa @ModelAttribute para evitar el error con la clase abstracta.
     */
    @PostMapping("/guardar")
    public String guardarUsuario(@RequestParam(value = "id", required = false) Long id,
                                 @RequestParam("nombres") String nombres,
                                 @RequestParam("apellidos") String apellidos,
                                 @RequestParam("email") String email,
                                 @RequestParam(value = "password", required = false) String password,
                                 @RequestParam(value = "rol", required = false) String rol,
                                 @RequestParam("numeroDocumento") String numeroDocumento,
                                 @RequestParam("fechaNacimiento") LocalDate fechaNacimiento,
                                 RedirectAttributes redirectAttributes) {

        Usuario usuario;
        // Si hay un ID, estamos editando. Si no, creando.
        if (id != null) {
            usuario = usuarioService.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        } else {
            // Al crear, determinamos la clase por el rol
            if ("ADMIN".equals(rol)) {
                usuario = new Administrador();
            } else {
                usuario = new Cliente();
            }
        }

        // Asignamos los valores del formulario al objeto
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setEmail(email);
        usuario.setNumeroDocumento(numeroDocumento);
        usuario.setFechaNacimiento(fechaNacimiento);

        // La contraseña solo se actualiza si se escribe algo en el campo
        if (password != null && !password.isEmpty()) {
            usuario.setPassword(password);
        }

        // Llamamos al servicio (que ya no necesita el rol como parámetro)
        usuarioService.saveOrUpdate(usuario, rol); // El rol solo se usa al crear

        redirectAttributes.addFlashAttribute("successMessage", "Usuario guardado exitosamente.");
        return "redirect:/admin/usuarios";
    }

    // ... (método eliminarUsuario no cambia) ...
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado.");
        return "redirect:/admin/usuarios";
    }
}