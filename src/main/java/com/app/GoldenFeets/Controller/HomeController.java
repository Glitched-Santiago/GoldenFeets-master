package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Asegúrate de tener esta importación
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor // Esta anotación inyecta el servicio en el constructor
public class HomeController {

    // 1. Inyectamos el servicio como un campo final
    private final ProductoService productoService;

    @GetMapping("/")
    // 2. Añadimos "Model model" como parámetro del método
    public String landingPage(Model model) {

        // Ahora el código funcionará porque "productoService" y "model" existen
        List<Producto> productosDestacados = productoService.encontrarProductosAleatorios(3);
        model.addAttribute("productosDestacados", productosDestacados);

        // Asegúrate de que tu archivo HTML se llame "landing.html"
        return "landing";
    }
}