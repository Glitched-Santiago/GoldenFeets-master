package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Repository.CategoriaRepository;
import com.app.GoldenFeets.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepository;

    // READ - Listar productos (con búsqueda)
    @GetMapping
    public String listarProductos(@RequestParam(required = false) String nombre, Model model) {
        if (nombre != null && !nombre.isEmpty()) {
            model.addAttribute("productos", productoService.buscarPorNombre(nombre));
        } else {
            model.addAttribute("productos", productoService.obtenerTodos());
        }
        // AÑADIDO: Indica a la plantilla qué página está activa.
        model.addAttribute("activePage", "inventario");
        return "inventario/inventario";
    }

    // CREATE - Mostrar formulario para nuevo producto
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaRepository.findAll());
        // AÑADIDO: Indica a la plantilla qué página está activa.
        model.addAttribute("activePage", "inventario");
        return "inventario/inventario-form";
    }

    // UPDATE - Mostrar formulario para editar producto
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.obtenerPorId(id).orElseThrow(() -> new IllegalArgumentException("ID de Producto no válido:" + id)));
        model.addAttribute("categorias", categoriaRepository.findAll());
        // AÑADIDO: Indica a la plantilla qué página está activa.
        model.addAttribute("activePage", "inventario");
        return "inventario/inventario-form";
    }

    // CREATE / UPDATE - Guardar producto
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute("producto") Producto producto) {
        productoService.guardar(producto);
        return "redirect:/admin/inventario";
    }

    // DELETE - Eliminar producto
    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        productoService.eliminar(id);
        return "redirect:/admin/inventario";
    }
}