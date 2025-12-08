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
        return "carrito/Carrito";
    }

    @PostMapping("/agregar/{productoId}")
    public String agregarAlCarrito(
            @PathVariable Long productoId,
            @RequestParam(name = "cantidad", defaultValue = "1") int cantidad,
            @RequestParam(name = "productoVarianteId", required = false) Long productoVarianteId,
            RedirectAttributes redirectAttributes) {

        if (productoVarianteId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor selecciona un Color y una Talla.");
            return "redirect:/catalogo/" + productoId;
        }

        Optional<Producto> productoOpt = productoService.obtenerPorId(productoId);
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();

            // Buscar la variante real en la lista del producto (evita consultar BD de nuevo si ya tenemos el producto cargado)
            ProductoVariante variante = producto.getVariantes().stream()
                    .filter(v -> v.getId().equals(productoVarianteId))
                    .findFirst()
                    .orElse(null);

            if (variante == null || !variante.getActivo()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Opción no disponible.");
                return "redirect:/catalogo/" + productoId;
            }

            // Validar Límite Global (Máximo 10 o Stock)
            int limiteReal = Math.min(10, variante.getStock());
            if (cantidad > limiteReal) {
                redirectAttributes.addFlashAttribute("errorMessage", "Solo puedes llevar máximo " + limiteReal + " unidades de este producto.");
                return "redirect:/catalogo/" + productoId;
            }

            carritoService.agregarProducto(producto, variante, cantidad);
            redirectAttributes.addFlashAttribute("successMessage", "¡Añadido a la bolsa!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado.");
        }

        return "redirect:/catalogo/" + productoId;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarDelCarrito(@PathVariable("id") Long productoVarianteId) {
        carritoService.removerProducto(productoVarianteId);
        return "redirect:/carrito";
    }

    @PostMapping("/actualizar")
    public String actualizarCantidad(@RequestParam("productoVarianteId") Long productoVarianteId,
                                     @RequestParam("cantidad") int cantidad) {
        // Validación básica en el controller (la lógica de stock debería estar en el servicio idealmente)
        if (cantidad < 1) cantidad = 1;
        // El límite máximo ya lo controla la vista, pero el servicio debería validar stock final

        carritoService.actualizarCantidad(productoVarianteId, cantidad);
        return "redirect:/carrito";
    }
}