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

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final PdfService pdfService;

    // Inyectamos el correo desde la configuración para evitar bloqueos por seguridad
    // Si usas Gmail, este debe ser tu correo de Gmail. Si usas Resend, el que ellos te den.
    @Value("${spring.mail.username}")
    private String remitente;

    @Async // <--- IMPORTANTE: Ejecuta el envío en segundo plano para no congelar la app
    public void enviarCorreoCompra(String destinatario, Pedido pedido) {
        try {
            logger.info("Iniciando proceso de envío de correo a: {}", destinatario);

            // 1. Configurar Mensaje
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setTo(destinatario);
            helper.setSubject("Tu Factura de Compra - Orden #" + pedido.getId());

            // Usamos la variable inyectada. Si es nula, usamos un fallback (pero idealmente no debería serlo)
            helper.setFrom(remitente != null ? remitente : "no-reply@goldenfeets.com");

            // 2. Preparar Datos (Contexto)
            Context context = new Context();
            context.setVariable("pedido", pedido);
            context.setVariable("cliente", pedido.getCliente());

            // 3. Generar PDF (Bytes)
            byte[] pdfBytes = pdfService.generarPdf("reportes/factura-compra", context);

            // 4. Generar Cuerpo HTML del Correo
            String htmlContent = templateEngine.process("emails/recibo-compra", context);
            helper.setText(htmlContent, true);

            // 5. Adjuntar el PDF
            helper.addAttachment("Factura_GoldenFeets_" + pedido.getId() + ".pdf", new ByteArrayResource(pdfBytes));

            // 6. Enviar
            javaMailSender.send(message);

            logger.info("Correo enviado exitosamente con PDF adjunto.");

        } catch (MessagingException e) {
            logger.error("Error al crear el mensaje de correo", e);
        } catch (Exception e) {
            logger.error("Error general enviando correo o generando PDF", e);
        }
    }

    @Async
    public void enviarCorreoMasivo(String asunto, String plantilla) {
        // Lógica futura...
    }
}