package com.app.GoldenFeets.Controller;

import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.Pedido;
import com.app.GoldenFeets.Service.CarritoService;
import com.app.GoldenFeets.Service.EmailService;
import com.app.GoldenFeets.Service.PedidoService;
import com.app.GoldenFeets.Service.UsuarioService;
import com.app.GoldenFeets.Model.CarritoItem;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final CarritoService carritoService;
    private final EmailService emailService;

    // --- 1. MOSTRAR CHECKOUT ---
    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, RedirectAttributes redirectAttributes) {
        Map<Long, CarritoItem> carrito = carritoService.getItems();

        // Filtrar vacíos
        Map<Long, CarritoItem> carritoValido = (carrito != null) ? carrito.entrySet().stream()
                .filter(entry -> entry.getValue().getCantidad() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)) : null;

        if (carritoValido == null || carritoValido.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tu carrito está vacío.");
            return "redirect:/carrito";
        }

        model.addAttribute("items", carritoValido.values());
        model.addAttribute("total", carritoService.getTotal());

        return "Compra/Checkout";
    }

    // --- 2. PROCESAR COMPRA (ACTUALIZADO CON ENVÍO DE CORREO) ---
    @PostMapping("/checkout")
    public String finalizarCompra(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("nombres") String nombres,
            @RequestParam("apellidos") String apellidos,
            @RequestParam("telefono") String telefono,
            @RequestParam("ciudad") String ciudad,
            @RequestParam("localidad") String localidad,
            @RequestParam("direccion") String direccion,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";

        Map<Long, CarritoItem> carrito = carritoService.getItems();
        if (carrito == null || carrito.isEmpty()) {
            return "redirect:/carrito";
        }

        try {
            Cliente cliente = usuarioService.findClienteByEmail(userDetails.getUsername());

            // 1. Crear el pedido en BD
            Pedido nuevoPedido = pedidoService.crearPedido(
                    carrito, cliente, nombres, apellidos, telefono, ciudad, localidad, direccion
            );

            // 2. ENVIAR CORREO DE CONFIRMACIÓN (Nuevo bloque)
            // Lo hacemos en un hilo secundario para no bloquear la redirección
            new Thread(() -> {
                try {
                    emailService.enviarCorreoCompra(cliente.getEmail(), nuevoPedido);
                    System.out.println("Correo de compra enviado a: " + cliente.getEmail());
                } catch (Exception e) {
                    System.err.println("Error enviando correo al cliente: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();

            // 3. Limpiar carrito y redirigir
            carritoService.limpiar();
            return "redirect:/pedidos/confirmacion/" + nuevoPedido.getId();

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/pedidos/checkout";
        }
    }

    // --- 3. CONFIRMACIÓN ---
    @GetMapping("/confirmacion/{pedidoId}")
    public String confirmacionCompra(@PathVariable Long pedidoId, Model model) {
        // Buscamos el pedido completo para mostrar los detalles en la factura
        Pedido pedido = pedidoService.obtenerPedidoPorId(pedidoId); // Asumo que tienes este método en tu servicio

        // Si no tienes ese método en el service, puedes usar el repositorio directamente:
        // Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();

        model.addAttribute("pedido", pedido);
        return "Compra/confirmacion";
    }

    // --- 4. HISTORIAL DE COMPRAS (CLIENTE) ---
    @GetMapping("/historial")
    public String verHistorialCompras(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        Cliente cliente = usuarioService.findClienteByEmail(userDetails.getUsername());
        List<Pedido> pedidos = pedidoService.obtenerPedidosPorCliente(cliente);
        model.addAttribute("pedidos", pedidos);
        return "Compra/compras";
    }
}