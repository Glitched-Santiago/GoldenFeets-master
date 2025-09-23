package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UsuarioService usuarioService; // Para obtener la lista de usuarios

    /**
     * Orquesta el envío masivo. Obtiene los destinatarios y llama al
     * método de envío asíncrono para cada uno.
     */
    public void enviarCorreoMasivo(String asunto, String nombrePlantilla) {
        // Obtenemos todos los usuarios (o podrías filtrarlos)
        List<Usuario> destinatarios = usuarioService.findAll();

        for (Usuario destinatario : destinatarios) {
            // Preparamos el contexto con las variables para la plantilla
            Context context = new Context();
            context.setVariable("nombreUsuario", destinatario.getNombres());
            // Puedes añadir más variables aquí (ofertas, noticias, etc.)

            // Procesamos la plantilla HTML con Thymeleaf
            String contenidoHtml = templateEngine.process("emails/" + nombrePlantilla, context);

            // Llamamos al método asíncrono para enviar el correo
            enviarCorreoHtmlAsync(destinatario.getEmail(), asunto, contenidoHtml);
        }
    }

    /**
     * Este método se ejecuta en un hilo separado gracias a @Async.
     * La aplicación principal no esperará a que se complete el envío.
     */
    @Async
    public void enviarCorreoHtmlAsync(String destinatario, String asunto, String contenidoHtml) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("tu-correo@gmail.com");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true); // true indica que el contenido es HTML
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Es una buena práctica registrar el error
            // logger.error("Error al enviar correo a {}: {}", destinatario, e.getMessage());
            e.printStackTrace();
        }
    }
}