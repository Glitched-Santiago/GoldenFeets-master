package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Pedido;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    // Logger para ver errores en la consola de Railway de forma ordenada
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final PdfService pdfService;

    // Inyectamos el remitente desde application.properties.
    // Si no está configurado, usa el de prueba de Resend por defecto.
    @Value("${spring.mail.properties.mail.from:onboarding@resend.dev}")
    private String remitente;

    /**
     * Envía un correo con la factura PDF adjunta.
     * Es asíncrono para no bloquear el hilo principal.
     */
    @Async
    public void enviarCorreoCompra(String destinatario, Pedido pedido) {
        try {
            logger.info("Iniciando envío de correo a: {}", destinatario);

            // 1. Configurar el Mensaje
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            // IMPORTANTE: En Resend, el 'From' debe coincidir con tu dominio verificado
            // o ser 'onboarding@resend.dev' si estás en modo prueba.
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("Tu Factura de Compra - Orden #" + pedido.getId());

            // 2. Preparar Datos (Contexto para Thymeleaf)
            Context context = new Context();
            context.setVariable("pedido", pedido);
            context.setVariable("cliente", pedido.getCliente());

            // 3. Generar PDF (Bytes)
            // Asegúrate de que la ruta 'reportes/factura-compra' exista en tus templates
            byte[] pdfBytes = pdfService.generarPdf("reportes/factura-compra", context);

            // 4. Generar Cuerpo HTML del Correo
            String htmlContent = templateEngine.process("emails/recibo-compra", context);
            helper.setText(htmlContent, true);

            // 5. Adjuntar el PDF
            helper.addAttachment("Factura_GoldenFeets_" + pedido.getId() + ".pdf", new ByteArrayResource(pdfBytes));

            // 6. Enviar
            javaMailSender.send(message);

            logger.info("Correo enviado exitosamente a {} con PDF adjunto.", destinatario);

        } catch (MessagingException e) {
            logger.error("Error construyendo el mensaje de correo para: {}", destinatario, e);
        } catch (Exception e) {
            logger.error("Error general (posiblemente al generar PDF) enviando correo a: {}", destinatario, e);
        }
    }

    @Async
    public void enviarCorreoMasivo(String asunto, String plantilla) {
        // Aquí puedes implementar lógica futura para newsletters
        logger.info("Método de correo masivo llamado (aún no implementado).");
    }
}