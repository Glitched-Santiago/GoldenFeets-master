package com.app.GoldenFeets.Service;

import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final TemplateEngine templateEngine;

    public byte[] generarPdf(String templateName, Context context) throws IOException, DocumentException {
        // 1. Procesamos la plantilla Thymeleaf para obtener el HTML final
        String html = templateEngine.process(templateName, context);

        // 2. Usamos Flying Saucer para convertir el HTML a PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream, false);
        renderer.finishPDF();
        outputStream.close();

        return outputStream.toByteArray();
    }
}
