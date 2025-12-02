package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Administrador;
import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.Usuario;
import com.app.GoldenFeets.Service.EmailService;
import com.app.GoldenFeets.Service.PdfService;
import com.app.GoldenFeets.Service.UploadFileService; // <--- OBLIGATORIO
import com.app.GoldenFeets.Service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // <--- OBLIGATORIO
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final EmailService emailService;
    private final PdfService pdfService;
    private final UploadFileService uploadService; // <--- INYECCIÓN

    // --- EXPORTAR PDF ---
    @GetMapping("/exportar-pdf")
    public void exportarPdf(@RequestParam(required = false) String id,
                            @RequestParam(required = false) String nombres,
                            @RequestParam(required = false) String apellidos,
                            @RequestParam(required = false) String numeroDocumento,
                            @RequestParam(required = false) String email,
                            @RequestParam(required = false) String rol,
                            HttpServletResponse response) {

        List<Usuario> usuarios = usuarioService.search(id, nombres, apellidos, numeroDocumento, email, rol);
        Context context = new Context();
        context.setVariable("usuarios", usuarios);
        context.setVariable("fechaReporte", LocalDate.now());
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_usuarios.pdf\"");
        try {
            byte[] pdfBytes = pdfService.generarPdf("reportes/reporte-usuarios", context);
            response.getOutputStream().write(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- LISTAR ---
    @GetMapping
    public String listarUsuarios(@RequestParam(required = false) String id,
                                 @RequestParam(required = false) String nombres,
                                 @RequestParam(required = false) String apellidos,
                                 @RequestParam(required = false) String numeroDocumento,
                                 @RequestParam(required = false) String email,
                                 @RequestParam(required = false) String rol,
                                 Model model) {
        model.addAttribute("usuarios", usuarioService.search(id, nombres, apellidos, numeroDocumento, email, rol));

        // Mantener filtros
        model.addAttribute("idParam", id);
        model.addAttribute("nombresParam", nombres);
        model.addAttribute("apellidosParam", apellidos);
        model.addAttribute("numeroDocumentoParam", numeroDocumento);
        model.addAttribute("emailParam", email);
        model.addAttribute("rolParam", rol);
        model.addAttribute("activePage", "usuarios");
        return "usuarios/usuarios";
    }

    // --- NUEVO USUARIO (GET) ---
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        // Usamos Cliente por defecto para instanciar el formulario
        model.addAttribute("usuario", new Cliente());
        model.addAttribute("isEditMode", false);
        model.addAttribute("activePage", "usuarios");
        return "usuarios/usuarios-form";
    }

    // --- EDITAR USUARIO (GET) ---
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
        usuario.setPassword(null); // Seguridad
        model.addAttribute("usuario", usuario);
        model.addAttribute("isEditMode", true);
        model.addAttribute("activePage", "usuarios");
        return "usuarios/usuarios-form";
    }

    // --- GUARDAR (POST) CON IMAGEN ---
    @PostMapping("/guardar")
    public String guardarUsuario(@RequestParam(value = "id", required = false) Long id,
                                 @RequestParam("nombres") String nombres,
                                 @RequestParam("apellidos") String apellidos,
                                 @RequestParam("email") String email,
                                 @RequestParam(value = "password", required = false) String password,
                                 @RequestParam(value = "rol", required = false) String rol,
                                 @RequestParam("numeroDocumento") String numeroDocumento,
                                 @RequestParam("fechaNacimiento") LocalDate fechaNacimiento,
                                 @RequestParam(value = "file", required = false) MultipartFile file, // <--- ARCHIVO
                                 RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario;
            if (id != null) {
                usuario = usuarioService.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            } else {
                if ("ADMIN".equals(rol)) {
                    usuario = new Administrador();
                } else {
                    usuario = new Cliente();
                }
            }

            usuario.setNombres(nombres);
            usuario.setApellidos(apellidos);
            usuario.setEmail(email);
            usuario.setNumeroDocumento(numeroDocumento);
            usuario.setFechaNacimiento(fechaNacimiento);

            if (password != null && !password.isEmpty()) {
                usuario.setPassword(password);
            }

            // --- PROCESAR IMAGEN ---
            if (file != null && !file.isEmpty()) {
                String nombreImagen = uploadService.saveImage(file);
                usuario.setFotoUrl("/images/" + nombreImagen);
            }

            // Guardar en BD
            usuarioService.saveOrUpdate(usuario, rol);

            redirectAttributes.addFlashAttribute("successMessage", "Usuario guardado exitosamente.");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    // --- ELIMINAR ---
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado.");
        return "redirect:/admin/usuarios";
    }

    // --- CORREO MASIVO ---
    @PostMapping("/enviar-promocion")
    public String enviarPromocion(RedirectAttributes redirectAttributes) {
        emailService.enviarCorreoMasivo("¡Nuevas Ofertas!", "plantilla-promocion");
        redirectAttributes.addFlashAttribute("successMessage", "Enviando correos...");
        return "redirect:/admin/usuarios";
    }
}