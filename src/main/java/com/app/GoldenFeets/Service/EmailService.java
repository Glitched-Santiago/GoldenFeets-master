package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Pedido;
import com.app.GoldenFeets.Entity.Usuario;
import com.app.GoldenFeets.Repository.UsuarioRepository;
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
import java.util.List;

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

    private final UsuarioRepository usuarioRepository;

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
    @Async
    public void enviarCorreoMasivo(String tipo, String asunto, String mensajeCuerpo) {
        try {
            logger.info("Iniciando envío masivo de tipo: {}", tipo);

            // 1. Determinar destinatarios y plantilla según el tipo
            List<Usuario> destinatarios;
            String plantilla;

            if ("PROMOCION".equalsIgnoreCase(tipo)) {
                destinatarios = usuarioRepository.findByRol("CLIENTE"); // Busca en BD
                plantilla = "emails/promocion";
            } else if ("NOTIFICACION".equalsIgnoreCase(tipo)) {
                destinatarios = usuarioRepository.findByRol("ADMIN"); // Busca en BD
                plantilla = "emails/notificacion";
            } else {
                logger.warn("Tipo de correo desconocido: {}", tipo);
                return;
            }

            if (destinatarios.isEmpty()) {
                logger.info("No se encontraron usuarios para el rol seleccionado.");
                return;
            }

            // 2. Iterar y enviar correos (Resend Free tiene límite de velocidad, hacerlo así es más seguro)
            for (Usuario usuario : destinatarios) {
                try {
                    Context context = new Context();
                    context.setVariable("nombre", usuario.getNombres()); // Personalización
                    context.setVariable("mensaje", mensajeCuerpo);

                    String htmlContent = templateEngine.process(plantilla, context);

                    CreateEmailOptions params = CreateEmailOptions.builder()
                            .from(remitente)
                            .to(usuario.getEmail())
                            .subject(asunto)
                            .html(htmlContent)
                            .build();

                    resendClient.emails().send(params);

                    // Pequeña pausa para no saturar la API en bucles grandes (opcional)
                    Thread.sleep(200);

                } catch (Exception e) {
                    logger.error("Error enviando correo a: " + usuario.getEmail(), e);
                    // Continuamos con el siguiente usuario aunque este falle
                }
            }
            logger.info("Proceso de envío masivo finalizado.");

        } catch (Exception e) {
            logger.error("Error general en el proceso de correo masivo", e);
        }
    }
}