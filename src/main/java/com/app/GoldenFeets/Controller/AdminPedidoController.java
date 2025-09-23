package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Pedido;
import com.app.GoldenFeets.Service.PdfService;
import com.app.GoldenFeets.Service.PedidoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/pedidos")
@RequiredArgsConstructor
public class AdminPedidoController {

    private final PedidoService pedidoService;
    private final PdfService pdfService;

    // ... (tu método listarPedidos no cambia) ...

    // --- AÑADIMOS ESTE NUEVO MÉTODO ---
    @GetMapping("/exportar-pdf")
    public void exportarPdf(
            @RequestParam(required = false) String clienteNombre,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            HttpServletResponse response) {

        // 1. Obtenemos los mismos datos que se muestran en la tabla
        List<Pedido> pedidos = pedidoService.buscarPedidos(clienteNombre, fechaDesde, fechaHasta);

        // 2. Preparamos el contexto para Thymeleaf
        Context context = new Context();
        context.setVariable("pedidos", pedidos);
        context.setVariable("fechaReporte", LocalDate.now());

        // 3. Configuramos la respuesta HTTP
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_pedidos.pdf\"");

        // 4. Generamos y escribimos el PDF en la respuesta
        try {
            byte[] pdfBytes = pdfService.generarPdf("reportes/reporte-pedidos", context);
            response.getOutputStream().write(pdfBytes);
        } catch (Exception e) {
            // Manejar la excepción (ej. loggear el error)
            e.printStackTrace();
        }
    }

    @GetMapping
    public String listarPedidos(
            @RequestParam(required = false) String clienteNombre,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Model model) {

        List<Pedido> pedidos;
        if (clienteNombre == null && fechaDesde == null && fechaHasta == null) {
            pedidos = pedidoService.obtenerTodosLosPedidos();
        } else {
            pedidos = pedidoService.buscarPedidos(clienteNombre, fechaDesde, fechaHasta);
        }

        // --- SINTAXIS CORREGIDA AQUÍ ---
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("activePage", "pedidos");

        // Pasamos los parámetros de búsqueda de vuelta a la vista
        model.addAttribute("clienteNombre", clienteNombre);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);

        return "compras-admin/compras";
    }
}