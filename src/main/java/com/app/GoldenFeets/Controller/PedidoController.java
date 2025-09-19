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

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    // Método seguro para obtener el carrito de la sesión del usuario.
    @SuppressWarnings("unchecked")
    private Map<Long, CarritoItem> getCarrito(HttpSession session) {
        return (Map<Long, CarritoItem>) session.getAttribute("CARRITO_SESSION");
    }

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

            // LLAMADA CORREGIDA: Pasamos el carrito y el cliente al servicio.
            Pedido nuevoPedido = pedidoService.crearPedido(carrito, cliente);

            // Limpiamos el carrito de la sesión después de la compra.
            session.removeAttribute("CARRITO_SESSION");

            return "redirect:/pedidos/confirmacion/" + nuevoPedido.getId();

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el pedido: " + e.getMessage());
            return "redirect:/carrito";
        }
    }

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

    @GetMapping("/confirmacion/{pedidoId}")
    public String confirmacionCompra(@PathVariable Long pedidoId, Model model) {
        model.addAttribute("pedidoId", pedidoId);
        return "compra/confirmacion";
    }
}