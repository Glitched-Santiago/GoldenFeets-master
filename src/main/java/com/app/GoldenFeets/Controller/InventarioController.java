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
import com.app.GoldenFeets.Service.UploadFileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final ProductoService productoService;
    private final PdfService pdfService;
    private final CategoriaRepository categoriaRepository;
    private final InventarioService inventarioService;
    private final ProductoVarianteRepository productoVarianteRepository;
    private final UploadFileService uploadService;

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

        model.addAttribute("keywordParam", keyword);
        model.addAttribute("categoriaIdParam", categoriaId);
        model.addAttribute("precioMinParam", precioMin);
        model.addAttribute("precioMaxParam", precioMax);
        model.addAttribute("tallaParam", talla);
        model.addAttribute("colorParam", color);

        return "inventario/inventario";
    }

    // --- ENTRADA DE MERCANCÍA ---
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
            if (entradaDto.getCantidad() == null || entradaDto.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
            }
            inventarioService.registrarEntrada(entradaDto);
            redirectAttributes.addFlashAttribute("successMessage", "Entrada registrada correctamente. Stock actualizado.");
            return "redirect:/admin/inventario/historial";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar entrada: " + e.getMessage());
            return "redirect:/admin/inventario/entrada";
        }
    }

    // --- SALIDA MANUAL ---
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
            inventarioService.registrarSalidaManual(salidaDto);
            redirectAttributes.addFlashAttribute("successMessage", "Salida registrada correctamente.");
            return "redirect:/admin/inventario/historial";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar salida: " + e.getMessage());
            return "redirect:/admin/inventario/salida";
        }
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

    // --- CRUD PRODUCTOS ---
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

        Map<String, List<ProductoVariante>> variantesPorColor = producto.getVariantes().stream()
                .collect(Collectors.groupingBy(ProductoVariante::getColor));

        model.addAttribute("producto", producto);
        model.addAttribute("variantesPorColor", variantesPorColor);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("activePage", "inventario");

        return "inventario/inventario-form";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto productoForm,
                                  @RequestParam(value = "file", required = false) MultipartFile file,
                                  RedirectAttributes redirectAttributes) {
        try {
            Producto productoAGuardar;
            if (productoForm.getId() != null) {
                productoAGuardar = productoService.obtenerPorId(productoForm.getId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                productoAGuardar.setNombre(productoForm.getNombre());
                productoAGuardar.setPrecio(productoForm.getPrecio());
                productoAGuardar.setDescripcion(productoForm.getDescripcion());
                productoAGuardar.setCategoria(productoForm.getCategoria());
            } else {
                productoAGuardar = productoForm;
            }

            if (file != null && !file.isEmpty()) {
                String nombreImagen = uploadService.saveImage(file);
                productoAGuardar.setImagenUrl("/images/" + nombreImagen);
            }

            Producto productoGuardado = productoService.guardar(productoAGuardar);

            redirectAttributes.addFlashAttribute("successMessage", "Producto guardado correctamente.");

            // --- CORRECCIÓN: Redirección correcta a la edición ---
            return "redirect:/admin/inventario/editar/" + productoGuardado.getId();

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar: " + e.getMessage());
            return "redirect:/admin/inventario";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        productoService.eliminar(id);
        return "redirect:/admin/inventario";
    }

    // --- VARIANTES ---
    @GetMapping("/variante/{id}/toggle")
    public String toggleVariante(@PathVariable Long id, HttpServletRequest request) {
        ProductoVariante v = productoVarianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variante no encontrada"));
        v.setActivo(!v.getActivo());
        productoVarianteRepository.save(v);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/inventario");
    }

    @PostMapping("/variante/agregar-manual")
    public String agregarVarianteManual(
            @RequestParam Long productoId,
            @RequestParam String talla,
            @RequestParam String color,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes) {

        try {
            String rutaImagen = null;
            if (file != null && !file.isEmpty()) {
                String nombreArchivo = uploadService.saveImage(file);
                rutaImagen = "/images/" + nombreArchivo;
            }

            productoService.crearVarianteManual(productoId, talla, color, rutaImagen);
            redirectAttributes.addFlashAttribute("successMessage", "Variante actualizada/creada correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/admin/inventario/editar/" + productoId;
    }

    @GetMapping("/variante/toggle-color")
    public String toggleColorCompleto(
            @RequestParam Long productoId,
            @RequestParam String color,
            HttpServletRequest request) {
        productoService.toggleVariantesPorColor(productoId, color);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/inventario");
    }

    @GetMapping("/variante/eliminar/{id}")
    public String eliminarVariante(
            @PathVariable Long id,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminarVariante(id);
            redirectAttributes.addFlashAttribute("successMessage", "Variante eliminada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar (posible uso en pedidos): " + e.getMessage());
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/inventario");
    }
}