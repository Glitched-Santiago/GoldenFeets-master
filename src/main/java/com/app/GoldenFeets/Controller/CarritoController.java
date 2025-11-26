package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Entity.ProductoVariante;
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

    private final CarritoService carritoService;
    private final ProductoService productoService;

    @GetMapping
    public String verCarrito(Model model) {
        model.addAttribute("items", carritoService.getItems().values());
        model.addAttribute("total", carritoService.getTotal());
        return "carrito/carrito"; // Asegúrate que esta vista existe
    }

    @PostMapping("/agregar/{productoId}")
    public String agregarAlCarrito(
            @PathVariable Long productoId,
            @RequestParam(name = "cantidad", defaultValue = "1") int cantidad,
            @RequestParam(name = "productoVarianteId", required = false) Long productoVarianteId,
            RedirectAttributes redirectAttributes) {

        // 1. Validar Selección
        if (productoVarianteId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor selecciona un Color y una Talla.");
            return "redirect:/catalogo/" + productoId;
        }

        Optional<Producto> productoOpt = productoService.obtenerPorId(productoId);
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();

            // 2. Buscar variante específica
            ProductoVariante variante = producto.getVariantes().stream()
                    .filter(v -> v.getId().equals(productoVarianteId))
                    .findFirst()
                    .orElse(null);

            if (variante == null || !variante.getActivo()) {
                redirectAttributes.addFlashAttribute("errorMessage", "La opción seleccionada no está disponible.");
                return "redirect:/catalogo/" + productoId;
            }

            // 3. Validar Stock
            if (variante.getStock() >= cantidad) {
                carritoService.agregarProducto(producto, variante, cantidad);
                redirectAttributes.addFlashAttribute("successMessage", "¡Añadido a la bolsa!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Stock insuficiente. Solo quedan " + variante.getStock() + " unidades.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado.");
        }

        // Volvemos al detalle para seguir comprando
        return "redirect:/catalogo/" + productoId;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarDelCarrito(@PathVariable("id") Long productoVarianteId) {
        // El ID que recibimos es el de la variante (clave del mapa)
        carritoService.removerProducto(productoVarianteId);
        return "redirect:/carrito";
    }

    @PostMapping("/actualizar")
    public String actualizarCantidad(@RequestParam("productoVarianteId") Long productoVarianteId,
                                     @RequestParam("cantidad") int cantidad) {
        // Aquí podrías añadir validación de stock nuevamente si quisieras ser muy estricto
        carritoService.actualizarCantidad(productoVarianteId, cantidad);
        return "redirect:/carrito";
    }
}