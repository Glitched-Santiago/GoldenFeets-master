package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.Pedido;
import com.app.GoldenFeets.Service.PedidoService;
import com.app.GoldenFeets.Service.UsuarioService;
import com.app.GoldenFeets.Model.CarritoItem;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pedidos") // Ruta base para todo el controlador
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    /**
     * Método seguro para obtener el carrito de la sesión del usuario.
     */
    @SuppressWarnings("unchecked")
    private Map<Long, CarritoItem> getCarrito(HttpSession session) {
        return (Map<Long, CarritoItem>) session.getAttribute("CARRITO_SESSION");
    }

    /**
     * Muestra la página de checkout (formulario de pago) con el resumen del carrito.
     * Este es el método que te redirige al formulario.
     */
    @GetMapping("/checkout")
    public String showCheckoutPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Map<Long, CarritoItem> carrito = getCarrito(session);

        // Filtramos para asegurar que solo procesamos items con cantidad > 0
        Map<Long, CarritoItem> carritoValido = (carrito != null) ? carrito.entrySet().stream()
                .filter(entry -> entry.getValue().getCantidad() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)) : null;

        // Si el carrito está vacío o no tiene items válidos, redirigimos
        if (carritoValido == null || carritoValido.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tu carrito está vacío para poder proceder al pago.");
            return "redirect:/carrito";
        }

        double total = carritoValido.values().stream().mapToDouble(CarritoItem::getSubtotal).sum();
        model.addAttribute("items", carritoValido.values());
        model.addAttribute("total", total);

        return "compra/checkout"; // Apunta a la vista del formulario de pago
    }

    /**
     * Procesa la finalización de la compra desde el formulario de checkout.
     */
    @PostMapping("/checkout")
    public String finalizarCompra(@AuthenticationPrincipal UserDetails userDetails, HttpSession session, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Map<Long, CarritoItem> carrito = getCarrito(session);
        if (carrito == null || carrito.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tu carrito está vacío.");
            return "redirect:/carrito";
        }

        try {
            Cliente cliente = usuarioService.findClienteByEmail(userDetails.getUsername());

            // Pasamos el carrito y el cliente al servicio para crear el pedido.
            Pedido nuevoPedido = pedidoService.crearPedido(carrito, cliente);

            // Limpiamos el carrito de la sesión después de una compra exitosa.
            session.removeAttribute("CARRITO_SESSION");

            return "redirect:/pedidos/confirmacion/" + nuevoPedido.getId();

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el pedido: " + e.getMessage());
            return "redirect:/carrito";
        }
    }

    /**
     * Muestra el historial de compras del usuario autenticado.
     */
    @GetMapping("/historial")
    public String verHistorialCompras(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Cliente cliente = usuarioService.findClienteByEmail(userDetails.getUsername());
        List<Pedido> pedidos = pedidoService.obtenerPedidosPorCliente(cliente);
        model.addAttribute("pedidos", pedidos);
        return "compra/compras";
    }

    /**
     * Muestra una página de confirmación después de una compra exitosa.
     */
    @GetMapping("/confirmacion/{pedidoId}")
    public String confirmacionCompra(@PathVariable Long pedidoId, Model model) {
        model.addAttribute("pedidoId", pedidoId);
        // Necesitas crear esta vista: templates/compra/confirmacion.html
        return "compra/confirmacion";
    }
}