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
    public String agregarAlCarrito(
            @PathVariable Long productoId,
            // 1. Aceptamos la cantidad desde el formulario
            @RequestParam(name = "cantidad", defaultValue = "1") int cantidad,
            // 2. Aceptamos el ID de la variante (Talla/Color) seleccionado
            @RequestParam(name = "productoVarianteId", required = false) Long productoVarianteId,
            RedirectAttributes redirectAttributes) {

        Optional<Producto> productoOpt = productoService.obtenerPorId(productoId);
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();

            // 3. Validamos que se haya seleccionado una variante
            if (productoVarianteId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Debes seleccionar una talla y color.");
                return "redirect:/catalogo/" + productoId;
            }

            // 4. Buscamos la variante específica (Talla/Color) dentro del producto
            ProductoVariante variante = producto.getVariantes().stream()
                    .filter(v -> v.getId().equals(productoVarianteId))
                    .findFirst()
                    .orElse(null);

            if (variante == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "La variante seleccionada no es válida.");
                return "redirect:/catalogo/" + productoId;
            }

            // 5. Verificamos que haya suficiente stock EN LA VARIANTE (no en el producto general)
            if (variante.getStock() >= cantidad) {
                // 6. Pasamos la variante al servicio (usando el nuevo método)
                carritoService.agregarProducto(producto, variante, cantidad);
                redirectAttributes.addFlashAttribute("successMessage", "¡Producto añadido al carrito!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No hay stock suficiente para la talla solicitada (" + variante.getStock() + " disponibles).");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado.");
        }
        // Redirige de vuelta a la página del producto desde donde se añadió
        return "redirect:/catalogo/" + productoId;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarDelCarrito(@PathVariable("id") Long productoVarianteId) {
        // El ID ahora corresponde a la clave del mapa en el servicio (Variante ID)
        carritoService.removerProducto(productoVarianteId);
        return "redirect:/carrito";
    }

    /**
     * Nuevo método para manejar la actualización de la cantidad de un producto.
     * Recibe el ID de la variante y la nueva cantidad.
     */
    @PostMapping("/actualizar")
    public String actualizarCantidad(@RequestParam("productoVarianteId") Long productoVarianteId,
                                     @RequestParam("cantidad") int cantidad) {

        // Nota: Si quisieras validar stock aquí, necesitarías inyectar ProductoRepository o similar
        // para buscar la variante por su ID y chequear stock vs cantidad nueva.

        carritoService.actualizarCantidad(productoVarianteId, cantidad);
        return "redirect:/carrito";
    }
}