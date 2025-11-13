package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.DTO.InventarioEntradaDTO;
import com.app.GoldenFeets.DTO.InventarioSalidaDTO;
import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Repository.CategoriaRepository;
import com.app.GoldenFeets.Service.InventarioService;
import com.app.GoldenFeets.Service.PdfService;
import com.app.GoldenFeets.Service.ProductoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;
import com.app.GoldenFeets.DTO.EstadisticasInventarioDTO;
import com.app.GoldenFeets.DTO.HistorialInventarioDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Collections;

@Controller
@RequestMapping("/admin/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final ProductoService productoService;
    private final PdfService pdfService;
    private final CategoriaRepository categoriaRepository;
    private final InventarioService inventarioService;

    // --- MÉTODO LISTAR CORREGIDO ---
    @GetMapping
    public String listarInventario(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String talla, // <-- Parámetro añadido
            @RequestParam(required = false) String color, // <-- Parámetro añadido
            Model model) {

        // Llamada correcta al método de búsqueda con TODOS los parámetros
        List<Producto> productos = productoService.search(keyword, categoriaId, precioMin, precioMax, talla, color);

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("activePage", "inventario");

        // Devolvemos TODOS los parámetros para mantenerlos en los campos de búsqueda
        model.addAttribute("keywordParam", keyword);
        model.addAttribute("categoriaIdParam", categoriaId);
        model.addAttribute("precioMinParam", precioMin);
        model.addAttribute("precioMaxParam", precioMax);
        model.addAttribute("tallaParam", talla); // <-- Parámetro añadido al modelo
        model.addAttribute("colorParam", color); // <-- Parámetro añadido al modelo

        return "inventario/inventario";
    }

    // --- MÉTODO DE EXPORTACIÓN CORREGIDO ---
    @GetMapping("/exportar-pdf")
    public void exportarPdf(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String talla, // <-- Parámetro añadido
            @RequestParam(required = false) String color, // <-- Parámetro añadido
            HttpServletResponse response) {

        // Llamada correcta al método de búsqueda con TODOS los parámetros
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
            // Considera un manejo de errores más robusto aquí
            e.printStackTrace();
        }
    }

    // --- MÉTODOS CRUD DE PRODUCTOS (SIN CAMBIOS) ---
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

    // --- MÉTODOS PARA ENTRADA DE INVENTARIO (SIN CAMBIOS) ---
    @GetMapping("/entrada")
    public String mostrarFormularioEntrada(Model model) {
        // --- NUEVO: Lista de Distribuidores ---
        // (¡Reemplaza esto con tu lista real o cárgala desde la BBDD!)
        List<String> distribuidores = Arrays.asList(
                "Nike Proveedores S.A.S",
                "Adidas Colombia Ltda.",
                "Puma Distribuidores",
                "Calzado Nacional S.A."
        );
        // --- FIN ---

        model.addAttribute("entradaDto", new InventarioEntradaDTO());
        model.addAttribute("productos", productoService.findAll());
        model.addAttribute("distribuidores", distribuidores); // <-- Añadido al modelo
        model.addAttribute("activePage", "inventario");
        return "inventario/inventario-entrada";
    }

    @PostMapping("/entrada")
    public String registrarEntrada(@ModelAttribute("entradaDto") InventarioEntradaDTO entradaDto) {
        try {
            inventarioService.registrarEntrada(entradaDto);
        } catch (Exception e) {
            // Aquí deberías manejar el error, por ejemplo, con un mensaje de error en la vista
            return "redirect:/admin/inventario/entrada?error=true";
        }
        return "redirect:/admin/inventario";
    }
    @GetMapping("/salida")
    public String mostrarFormularioSalida(Model model) {
        // --- NUEVO: Lista de Motivos ---
        List<String> motivos = Arrays.asList(
                "Ajuste por pérdida",
                "Producto dañado",
                "Devolución a proveedor",
                "Uso interno"
        );
        // "Venta" NO se incluye aquí, ya que se registra automáticamente.
        // --- FIN ---

        model.addAttribute("salidaDto", new InventarioSalidaDTO());
        model.addAttribute("productos", productoService.findAll());
        model.addAttribute("motivos", motivos); // <-- Añadido al modelo
        model.addAttribute("activePage", "inventario");
        return "inventario/inventario-salida";
    }

    @PostMapping("/salida")
    public String registrarSalida(@ModelAttribute("salidaDto") InventarioSalidaDTO salidaDto) {
        try {
            inventarioService.registrarSalidaManual(salidaDto);
        } catch (Exception e) {
            // Manejar error (ej. stock insuficiente)
            return "redirect:/admin/inventario/salida?error=" + e.getMessage();
        }
        return "redirect:/admin/inventario";
    }
    @GetMapping("/historial")
    public String mostrarHistorial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String productoNombre,
            @RequestParam(required = false) String tipo,
            Model model) {

        // --- ¡AQUÍ ESTÁ LA TRAMPA! ---
        try {
            System.out.println("--- DEBUG (Controlador): Buscando Estadísticas ---");

            EstadisticasInventarioDTO estadisticas = inventarioService.getEstadisticasInventario(fechaDesde, fechaHasta);
            model.addAttribute("estadisticas", estadisticas);
            System.out.println("--- DEBUG (Controlador): ¡Estadísticas Encontradas! ---");


            System.out.println("--- DEBUG (Controlador): Buscando Historial ---");
            List<HistorialInventarioDTO> historial = inventarioService.getHistorialUnificado(fechaDesde, fechaHasta, productoNombre, tipo);
            model.addAttribute("historial", historial);
            System.out.println("--- DEBUG (Controlador): ¡Historial Encontrado! ---");

        } catch (Exception e) {
            // --- ¡EL SAPO! ---
            // ¡SI ALGO FALLA, ESTO IMPRIMIRÁ EL CHISME COMPLETO EN LA CONSOLA!
            System.err.println("¡NO JODA, SE REVENTÓ EL CONTROLADOR!");
            System.err.println("¡AQUÍ ESTÁ EL ERROR COMPLETO, MI PANA!");
            e.printStackTrace(); // <-- ¡ESTA ES LA LÍNEA MÁS IMPORTANTE!
            System.err.println("¡BUSCA ESTE ERROR ROJO EN LA CONSOLA DE INTELLIJ!");

            // Ponemos valores vacíos para que la página al menos intente cargar
            // aunque lo más seguro es que igual salga el 500, pero ya tendremos el error.
            model.addAttribute("estadisticas", new EstadisticasInventarioDTO());
            model.addAttribute("historial", Collections.emptyList());

            // Volvemos a lanzar el error para que veas el 500,
            // pero ya el chisme quedó en la consola.
            throw new RuntimeException("Error en mostrarHistorial, revisa la consola", e);
        }
        // --- FIN DE LA TRAMPA ---


        model.addAttribute("activePage", "inventario");

        // Devolver parámetros a la vista
        model.addAttribute("fechaDesdeParam", fechaDesde);
        model.addAttribute("fechaHastaParam", fechaHasta);
        model.addAttribute("productoNombreParam", productoNombre);
        model.addAttribute("tipoParam", tipo);

        return "inventario/inventario-historial";
    }
}

