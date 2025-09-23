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
            Model model) {

        boolean isFilterActive = (keyword != null && !keyword.isEmpty()) || categoriaId != null || precioMin != null || precioMax != null;

        if (isFilterActive) {
            model.addAttribute("productos", productoService.search(keyword, categoriaId, precioMin, precioMax));
        } else {
            // --- CORRECCIÓN AQUÍ ---
            // Por defecto, llamamos al método para obtener solo productos con stock
            model.addAttribute("productos", productoService.obtenerProductosDisponibles());
        }

        model.addAttribute("categorias", categoriaRepository.findAll());
        // Pasamos los filtros a la vista para mantenerlos en los campos
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMax", precioMax);

        return "catalogo"; // Asumiendo que tu vista se llama catalogo.html
    }
}