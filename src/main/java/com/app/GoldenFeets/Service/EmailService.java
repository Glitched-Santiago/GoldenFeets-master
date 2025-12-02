package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Pedido;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final PdfService pdfService; // <--- 1. Inyectamos el servicio PDF

    public void enviarCorreoCompra(String destinatario, Pedido pedido) {
        try {
            // 1. Configurar Mensaje
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setTo(destinatario);
            helper.setSubject("Tu Factura de Compra - Orden #" + pedido.getId());
            helper.setFrom("no-reply@goldenfeets.com");

            // 2. Preparar Datos (Contexto)
            Context context = new Context();
            context.setVariable("pedido", pedido);
            context.setVariable("cliente", pedido.getCliente());

            // 3. Generar PDF (Bytes)
            // Usa la plantilla XHTML estricta que acabamos de crear
            byte[] pdfBytes = pdfService.generarPdf("reportes/factura-compra", context);

            // 4. Generar Cuerpo HTML del Correo (El mensaje bonito)
            String htmlContent = templateEngine.process("emails/recibo-compra", context);
            helper.setText(htmlContent, true);

            // 5. Adjuntar el PDF
            helper.addAttachment("Factura_GoldenFeets_" + pedido.getId() + ".pdf", new ByteArrayResource(pdfBytes));

            // 6. Enviar
            javaMailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error enviando correo");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando el PDF");
        }
    }
    // Tu método existente de enviarCorreoMasivo se mantiene igual...
    public void enviarCorreoMasivo(String asunto, String plantilla) {
        // ... (Tu lógica actual)
    }
}