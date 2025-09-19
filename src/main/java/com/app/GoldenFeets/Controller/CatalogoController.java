package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

    private final ProductoService productoService;

    @GetMapping
    public String mostrarCatalogo(Model model) {
        model.addAttribute("productos", productoService.obtenerProductosDisponibles());
        // Ya no necesitamos pasar la autenticación. El ControllerAdvice lo hace por nosotros.
        return "catalogo/catalogo";
    }
}