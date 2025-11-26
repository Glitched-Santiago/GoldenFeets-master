package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Repository.CategoriaRepository;
import com.app.GoldenFeets.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public String mostrarCatalogo(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String talla,
            @RequestParam(required = false) String color,
            Model model) {

        boolean isFilterActive = (keyword != null && !keyword.isEmpty()) ||
                categoriaId != null || precioMin != null || precioMax != null ||
                (talla != null && !talla.isEmpty()) || (color != null && !color.isEmpty());

        List<Producto> productos;

        if (isFilterActive) {
            productos = productoService.search(keyword, categoriaId, precioMin, precioMax, talla, color);
        } else {
            productos = productoService.obtenerProductosDisponibles();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("activePage", "catalogo");

        // Mantener valores en los filtros
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMax", precioMax);
        model.addAttribute("talla", talla);
        model.addAttribute("color", color);

        return "catalogo/catalogo";
    }

    @GetMapping("/{id}")
    public String verDetalleProducto(@PathVariable("id") Long id, Model model) {
        Producto producto = productoService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));

        model.addAttribute("producto", producto);
        return "catalogo/detalle";
    }
}