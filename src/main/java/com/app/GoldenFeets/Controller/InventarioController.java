package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.DTO.EstadisticasInventarioDTO;
import com.app.GoldenFeets.DTO.HistorialInventarioDTO;
import com.app.GoldenFeets.DTO.InventarioEntradaDTO;
import com.app.GoldenFeets.DTO.InventarioSalidaDTO;
import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Entity.ProductoVariante;
import com.app.GoldenFeets.Repository.CategoriaRepository;
import com.app.GoldenFeets.Repository.ProductoVarianteRepository;
import com.app.GoldenFeets.Service.InventarioService;
import com.app.GoldenFeets.Service.PdfService;
import com.app.GoldenFeets.Service.ProductoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.app.GoldenFeets.Entity.ProductoVariante;

@Controller
@RequestMapping("/admin/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final ProductoService productoService;
    private final PdfService pdfService;
    private final CategoriaRepository categoriaRepository;
    private final InventarioService inventarioService;
    private final ProductoVarianteRepository productoVarianteRepository; // Inyecta esto

    // --- LISTAR INVENTARIO ---
    @GetMapping
    public String listarInventario(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String talla,
            @RequestParam(required = false) String color,
            Model model) {

        List<Producto> productos = productoService.search(keyword, categoriaId, precioMin, precioMax, talla, color);

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("activePage", "inventario");

        // Mantener filtros en la vista
        model.addAttribute("keywordParam", keyword);
        model.addAttribute("categoriaIdParam", categoriaId);
        model.addAttribute("precioMinParam", precioMin);
        model.addAttribute("precioMaxParam", precioMax);
        model.addAttribute("tallaParam", talla);
        model.addAttribute("colorParam", color);

        return "inventario/inventario";
    }

    // --- ENTRADA DE MERCANCÍA (NUEVO STOCK) ---
    @GetMapping("/entrada")
    public String mostrarFormularioEntrada(Model model) {
        List<String> distribuidores = Arrays.asList(
                "Nike Proveedores S.A.S", "Adidas Colombia Ltda.", "Puma Distribuidores", "Calzado Nacional S.A."
        );

        model.addAttribute("entradaDto", new InventarioEntradaDTO());
        model.addAttribute("productos", productoService.findAll());
        model.addAttribute("distribuidores", distribuidores);
        model.addAttribute("activePage", "inventario");
        return "inventario/inventario-entrada";
    }

    @PostMapping("/entrada")
    public String registrarEntrada(
            @ModelAttribute("entradaDto") InventarioEntradaDTO entradaDto,
            RedirectAttributes redirectAttributes) {
        try {
            // Validaciones básicas antes de llamar al servicio
            if (entradaDto.getCantidad() == null || entradaDto.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
            }
            // Llamamos al servicio (que ahora crea/actualiza Variantes)
            inventarioService.registrarEntrada(entradaDto);

            redirectAttributes.addFlashAttribute("successMessage", "Entrada registrada correctamente. Stock actualizado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar entrada: " + e.getMessage());
            return "redirect:/admin/inventario/entrada";
        }
        return "redirect:/admin/inventario";
    }

    // --- SALIDA MANUAL (AJUSTES) ---
    @GetMapping("/salida")
    public String mostrarFormularioSalida(Model model) {
        List<String> motivos = Arrays.asList("Ajuste por pérdida", "Producto dañado", "Devolución a proveedor", "Uso interno");

        model.addAttribute("salidaDto", new InventarioSalidaDTO());
        model.addAttribute("productos", productoService.findAll());
        model.addAttribute("motivos", motivos);
        model.addAttribute("activePage", "inventario");
        return "inventario/inventario-salida";
    }

    @PostMapping("/salida")
    public String registrarSalida(
            @ModelAttribute("salidaDto") InventarioSalidaDTO salidaDto,
            RedirectAttributes redirectAttributes) {
        try {
            if (salidaDto.getCantidad() == null || salidaDto.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser positiva.");
            }
            // Nota: El usuario debe haber ingresado Talla/Color en el formulario para que esto funcione
            inventarioService.registrarSalidaManual(salidaDto);

            redirectAttributes.addFlashAttribute("successMessage", "Salida registrada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar salida: " + e.getMessage());
            return "redirect:/admin/inventario/salida";
        }
        return "redirect:/admin/inventario";
    }

    // --- HISTORIAL ---
    @GetMapping("/historial")
    public String mostrarHistorial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String productoNombre,
            @RequestParam(required = false) String tipo,
            Model model) {

        try {
            EstadisticasInventarioDTO estadisticas = inventarioService.getEstadisticasInventario(fechaDesde, fechaHasta);
            model.addAttribute("estadisticas", estadisticas);

            List<HistorialInventarioDTO> historial = inventarioService.getHistorialUnificado(fechaDesde, fechaHasta, productoNombre, tipo);
            model.addAttribute("historial", historial);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("estadisticas", new EstadisticasInventarioDTO());
            model.addAttribute("historial", Collections.emptyList());
            model.addAttribute("errorMessage", "Error al cargar el historial: " + e.getMessage());
        }

        model.addAttribute("activePage", "inventario");
        model.addAttribute("fechaDesdeParam", fechaDesde);
        model.addAttribute("fechaHastaParam", fechaHasta);
        model.addAttribute("productoNombreParam", productoNombre);
        model.addAttribute("tipoParam", tipo);

        return "inventario/inventario-historial";
    }

    // --- EXPORTAR PDF ---
    @GetMapping("/exportar-pdf")
    public void exportarPdf(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String talla,
            @RequestParam(required = false) String color,
            HttpServletResponse response) {

        List<Producto> productos = productoService.search(keyword, categoriaId, precioMin, precioMax, talla, color);

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

    // Métodos CRUD básicos (nuevo, editar, guardar, eliminar) se mantienen igual...
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("activePage", "inventario");
        return "inventario/inventario-form";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Producto producto = productoService.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido"));

        // 1. LOGICA NUEVA: Agrupar variantes por Color usando Java Streams
        Map<String, List<ProductoVariante>> variantesPorColor = producto.getVariantes().stream()
                .collect(Collectors.groupingBy(ProductoVariante::getColor));

        model.addAttribute("producto", producto);
        model.addAttribute("variantesPorColor", variantesPorColor); // <--- ESTO ES LA CLAVE
        model.addAttribute("categorias", categoriaRepository.findAll());

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
    @GetMapping("/variante/{id}/toggle")
    public String toggleVariante(@PathVariable Long id, HttpServletRequest request) {
        ProductoVariante v = productoVarianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variante no encontrada"));

        // Invertir estado (Si es true pasa a false, y viceversa)
        v.setActivo(!v.getActivo());
        productoVarianteRepository.save(v);

        // Redirigir a la página anterior (la de edición del producto)
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/inventario");
    }
    @PostMapping("/variante/agregar-manual")
    public String agregarVarianteManual(
            @RequestParam Long productoId,
            @RequestParam String talla,
            @RequestParam String color,
            @RequestParam(required = false) String imagenUrl, // <--- IMPORTANTE: Debe llamarse igual que en el HTML
            jakarta.servlet.http.HttpServletRequest request,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        try {
            // Imprimir en consola para verificar que llega el dato
            System.out.println("Recibiendo variante: Talla=" + talla + " Color=" + color + " Img=" + imagenUrl);

            productoService.crearVarianteManual(productoId, talla, color, imagenUrl);

            redirectAttributes.addFlashAttribute("successMessage", "Variante procesada correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/inventario");
    }
    @GetMapping("/variante/toggle-color")
    public String toggleColorCompleto(
            @RequestParam Long productoId,
            @RequestParam String color,
            jakarta.servlet.http.HttpServletRequest request) {

        productoService.toggleVariantesPorColor(productoId, color);

        // Recargar la página actual
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/inventario");
    }
    @GetMapping("/variante/eliminar/{id}")
    public String eliminarVariante(
            @PathVariable Long id,
            jakarta.servlet.http.HttpServletRequest request,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        try {
            productoService.eliminarVariante(id);
            redirectAttributes.addFlashAttribute("successMessage", "Variante eliminada correctamente.");
        } catch (Exception e) {
            // Capturamos error si la variante ya tiene ventas asociadas
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo eliminar: Es probable que esta variante ya tenga ventas o historial asociado. Intenta ocultarla en su lugar.");
        }

        // Redirigir a la misma página
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/inventario");
    }
}