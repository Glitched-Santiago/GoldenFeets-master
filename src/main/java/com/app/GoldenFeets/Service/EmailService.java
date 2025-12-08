package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Pedido;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.Attachment;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.annotation.PostConstruct;
import java.util.Base64;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final TemplateEngine templateEngine;
    private final PdfService pdfService;

    @Value("${spring.mail.password}")
    private String resendApiKey;

    @Value("${spring.mail.properties.mail.from:onboarding@resend.dev}")
    private String remitente;

    private Resend resendClient;

    @PostConstruct
    public void init() {
        this.resendClient = new Resend(resendApiKey);
    }

    @Async
    public void enviarCorreoCompra(String destinatario, Pedido pedido) {
        try {
            logger.info("Iniciando envío de correo API a: {}", destinatario);

            Context context = new Context();
            context.setVariable("pedido", pedido);
            context.setVariable("cliente", pedido.getCliente());
            byte[] pdfBytes = pdfService.generarPdf("reportes/factura-compra", context);

            String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

            Attachment adjunto = Attachment.builder()
                    .fileName("Factura_GoldenFeets_" + pedido.getId() + ".pdf")
                    .content(pdfBase64)
                    .build();

            String htmlContent = templateEngine.process("emails/recibo-compra", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(remitente)
                    .to(destinatario)
                    .subject("Tu Factura de Compra - Orden #" + pedido.getId())
                    .html(htmlContent)
                    .attachments(Collections.singletonList(adjunto))
                    .build();

            resendClient.emails().send(params);

            logger.info("Correo enviado exitosamente vía API a {}", destinatario);

        } catch (ResendException e) {
            logger.error("Error de Resend API: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error general enviando correo a: {}", destinatario, e);
        }
    }

    // --- MÉTODO AGREGADO PARA CORREGIR EL ERROR DE COMPILACIÓN ---
    @Async
    public void enviarCorreoMasivo(String asunto, String cuerpo) {
        try {
            logger.info("Procesando envío masivo: {}", asunto);

            // LÓGICA DE EJEMPLO (Enviaremos solo a ti mismo para probar, ya que Resend
            // no permite envíos masivos reales en el plan gratuito sin dominio verificado)

            // CreateEmailOptions params = CreateEmailOptions.builder()
            //         .from(remitente)
            //         .to("tu_correo_de_pruebas@gmail.com")
            //         .subject(asunto)
            //         .html("<p>" + cuerpo + "</p>")
            //         .build();
            // resendClient.emails().send(params);

            logger.warn("El método enviarCorreoMasivo fue llamado pero está en modo simulación.");

        } catch (Exception e) {
            logger.error("Error en envío masivo", e);
        }
    }
}