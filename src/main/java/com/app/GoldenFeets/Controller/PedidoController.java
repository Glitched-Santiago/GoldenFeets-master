package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.Pedido;
// --- PASO 1: AÑADIMOS LA IMPORTACIÓN DE CarritoService ---
import com.app.GoldenFeets.Service.CarritoService;
import com.app.GoldenFeets.Service.PedidoService;
import com.app.GoldenFeets.Service.UsuarioService;
import com.app.GoldenFeets.Model.CarritoItem;
// --- PASO 2: ELIMINAMOS LA IMPORTACIÓN DE HttpSession ---
// import jakarta.servlet.http.HttpSession;
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
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    // --- PASO 3: INYECTAMOS EL CarritoService ---
    private final CarritoService carritoService;

    @GetMapping("/checkout")
    // --- PASO 5: ELIMINAMOS HttpSession de los parámetros ---
    public String showCheckoutPage(Model model, RedirectAttributes redirectAttributes) {
        // Obtenemos el carrito directamente desde el servicio inyectado
        Map<Long, CarritoItem> carrito = carritoService.getItems();

        Map<Long, CarritoItem> carritoValido = (carrito != null) ? carrito.entrySet().stream()
                .filter(entry -> entry.getValue().getCantidad() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)) : null;

        if (carritoValido == null || carritoValido.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tu carrito está vacío para poder proceder al pago.");
            return "redirect:/carrito";
        }

        // Usamos el total del servicio para mayor consistencia
        model.addAttribute("items", carritoValido.values());
        model.addAttribute("total", carritoService.getTotal());

        return "compra/checkout";
    }

    @PostMapping("/checkout")
    // --- PASO 6: ELIMINAMOS HttpSession de los parámetros ---
    public String finalizarCompra(@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Obtenemos el carrito desde el servicio
        Map<Long, CarritoItem> carrito = carritoService.getItems();
        if (carrito == null || carrito.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tu carrito está vacío.");
            return "redirect:/carrito";
        }

        try {
            Cliente cliente = usuarioService.findClienteByEmail(userDetails.getUsername());
            Pedido nuevoPedido = pedidoService.crearPedido(carrito, cliente);

            // Limpiamos el carrito a través del servicio
            carritoService.limpiar();

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