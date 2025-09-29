package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Repository.CategoriaRepository;
import com.app.GoldenFeets.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/catalogo") // Asumo que esta es la ruta base del catálogo
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
            @RequestParam(required = false) String talla, // Parámetro añadido
            @RequestParam(required = false) String color, // Parámetro añadido
            Model model) {

        boolean isFilterActive = (keyword != null && !keyword.isEmpty()) ||
                categoriaId != null || precioMin != null || precioMax != null ||
                (talla != null && !talla.isEmpty()) || (color != null && !color.isEmpty());

        List<Producto> productos;
        if (isFilterActive) {
            // --- CORRECCIÓN AQUÍ ---
            // Llamada correcta al método de búsqueda con TODOS los parámetros
            productos = productoService.search(keyword, categoriaId, precioMin, precioMax, talla, color);
        } else {
            // Por defecto, mostramos solo los productos con stock disponible
            productos = productoService.obtenerProductosDisponibles();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("activePage", "catalogo"); // Para el menú activo

        // Devolvemos TODOS los parámetros para mantener los filtros en la vista
        model.addAttribute("keywordParam", keyword);
        model.addAttribute("categoriaIdParam", categoriaId);
        model.addAttribute("precioMinParam", precioMin);
        model.addAttribute("precioMaxParam", precioMax);
        model.addAttribute("tallaParam", talla);
        model.addAttribute("colorParam", color);

        // Asumo que tu vista se llama 'catalogo.html' dentro de una carpeta 'catalogo'
        return "catalogo/catalogo";
    }

    // Aquí irían otros métodos del controlador de catálogo, como la vista de detalle del producto, etc.
}
