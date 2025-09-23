package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Categoria; // Necesario para el formulario
import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Repository.CategoriaRepository;
import com.app.GoldenFeets.Service.PdfService;
import com.app.GoldenFeets.Service.ProductoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final ProductoService productoService;
    private final PdfService pdfService;
    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public String listarInventario(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            Model model) {

        boolean isFilterActive = (keyword != null && !keyword.isEmpty()) || categoriaId != null || precioMin != null || precioMax != null;

        List<Producto> productos;
        if (isFilterActive) {
            // Llamada correcta al método de búsqueda multicriterio
            productos = productoService.search(keyword, categoriaId, precioMin, precioMax);
        } else {
            // Llamada correcta para obtener todos los productos
            productos = productoService.findAll();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("activePage", "inventario");

        // Devolvemos los parámetros para mantenerlos en los campos de búsqueda
        model.addAttribute("keywordParam", keyword);
        model.addAttribute("categoriaIdParam", categoriaId);
        model.addAttribute("precioMinParam", precioMin);
        model.addAttribute("precioMaxParam", precioMax);

        return "inventario/inventario";
    }

    // --- MÉTODO DE EXPORTACIÓN CORREGIDO ---
    @GetMapping("/exportar-pdf")
    public void exportarPdf(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            HttpServletResponse response) {

        // AHORA USAMOS EL MISMO MÉTODO DE BÚSQUEDA QUE LA PÁGINA
        List<Producto> productos = productoService.search(keyword, categoriaId, precioMin, precioMax);

        Context context = new Context();
        context.setVariable("productos", productos);
        context.setVariable("fechaReporte", LocalDate.now());

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_inventario.pdf\"");

        try {
            byte[] pdfBytes = pdfService.generarPdf("reportes/reporte-inventario", context);
            response.getOutputStream().write(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODOS CRUD (sin cambios) ---
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("activePage", "inventario");
        return "inventario/inventario-form";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.obtenerPorId(id).orElseThrow(() -> new IllegalArgumentException("ID de Producto no válido:" + id)));
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("activePage", "inventario");
        return "inventario/inventario-form";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute("producto") Producto producto) {
        productoService.guardar(producto);
        return "redirect:/admin/inventario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        productoService.eliminar(id);
        return "redirect:/admin/inventario";
    }
}