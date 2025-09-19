package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Service.CarritoService;
import com.app.GoldenFeets.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/carrito")
@RequiredArgsConstructor
public class CarritoController {

    // Spring inyectará el proxy del CarritoService correcto para la sesión del usuario.
    private final CarritoService carritoService;
    private final ProductoService productoService;

    @GetMapping
    public String verCarrito(Model model) {
        model.addAttribute("items", carritoService.getItems().values());
        model.addAttribute("total", carritoService.getTotal());
        return "carrito/carrito";
    }

    @PostMapping("/agregar/{productoId}")
    public String agregarAlCarrito(@PathVariable Long productoId, RedirectAttributes redirectAttributes) {
        Optional<Producto> productoOpt = productoService.obtenerPorId(productoId);
        if (productoOpt.isPresent()) {
            if (productoOpt.get().getStock() > 0) {
                carritoService.agregarProducto(productoOpt.get(), 1);
                redirectAttributes.addFlashAttribute("successMessage", "¡Producto añadido al carrito!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Producto sin stock.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado.");
        }
        return "redirect:/catalogo";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarDelCarrito(@PathVariable("id") Long productoId) {
        carritoService.removerProducto(productoId);
        return "redirect:/carrito";
    }

    /**
     * Nuevo método para manejar la actualización de la cantidad de un producto.
     * Recibe el ID del producto y la nueva cantidad desde el formulario en la vista del carrito.
     */
    @PostMapping("/actualizar")
    public String actualizarCantidad(@RequestParam("productoId") Long productoId,
                                     @RequestParam("cantidad") int cantidad) {

        // Aquí podrías añadir una validación para asegurar que la cantidad no exceda el stock.
        // Por ejemplo:
        // Optional<Producto> p = productoService.obtenerPorId(productoId);
        // if (p.isPresent() && cantidad > p.get().getStock()) {
        //     // Manejar el error, por ejemplo, con un RedirectAttribute
        //     return "redirect:/carrito";
        // }

        carritoService.actualizarCantidad(productoId, cantidad);
        return "redirect:/carrito";
    }
}