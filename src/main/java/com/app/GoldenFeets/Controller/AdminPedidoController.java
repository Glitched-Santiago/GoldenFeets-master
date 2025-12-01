package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Pedido;
import com.app.GoldenFeets.Repository.PedidoRepository;
import com.app.GoldenFeets.Service.PdfService;
import com.app.GoldenFeets.Service.PedidoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final PedidoRepository pedidoRepository;

    // --- 1. EXPORTAR PDF ---
    @GetMapping("/exportar-pdf")
    public void exportarPdf(
            @RequestParam(required = false) String clienteNombre,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            HttpServletResponse response) {

        List<Pedido> pedidos = pedidoService.buscarPedidos(clienteNombre, fechaDesde, fechaHasta);

        Context context = new Context();
        context.setVariable("pedidos", pedidos);
        context.setVariable("fechaReporte", LocalDate.now());

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_pedidos.pdf\"");

        try {
            byte[] pdfBytes = pdfService.generarPdf("reportes/reporte-pedidos", context);
            response.getOutputStream().write(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 2. LISTAR PEDIDOS (Búsqueda y Filtros) ---
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

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("activePage", "pedidos");

        // Mantener filtros en el formulario
        model.addAttribute("clienteNombre", clienteNombre);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);

        return "compras-admin/compras";
    }

    // --- 3. VER DETALLE (CORREGIDO) ---
    // Antes tenías "/pedidos/{id}", lo que duplicaba la ruta.
    // Ahora es solo "/{id}" porque la clase ya tiene "/admin/pedidos"
    @GetMapping("/{id}")
    public String verDetallePedido(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));

        model.addAttribute("pedido", pedido);
        // Según tu estructura de carpetas: templates/compras-admin/pedido_detalle.html
        return "compras-admin/pedido_detalle";
    }
}