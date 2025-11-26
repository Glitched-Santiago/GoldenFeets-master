package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    }