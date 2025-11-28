package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.*;
import com.app.GoldenFeets.Exceptions.StockInsuficienteException;
import com.app.GoldenFeets.Model.CarritoItem;
import com.app.GoldenFeets.Repository.PedidoRepository;
import com.app.GoldenFeets.Repository.ProductoRepository;
import com.app.GoldenFeets.Repository.ProductoVarianteRepository;
import com.app.GoldenFeets.Repository.spec.PedidoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ProductoVarianteRepository productoVarianteRepository;
    private final PedidoSpecification pedidoSpecification;
    private final InventarioService inventarioService;

    @Transactional
    public Pedido crearPedido(Map<Long, CarritoItem> carrito, Cliente cliente) {
        if (carrito == null || carrito.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío.");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.PAGADO);

        for (CarritoItem item : carrito.values()) {
            // 1. Validar que el item tenga el ID de variante (Crucial para la nueva lógica)
            if (item.getProductoVarianteId() == null) {
                throw new RuntimeException("Error en datos del carrito: Falta ID de variante para " + item.getNombre());
            }

            // 2. BUSCAR POR ID EXACTO (Solución al error "Non unique result")
            ProductoVariante variante = productoVarianteRepository.findById(item.getProductoVarianteId())
                    .orElseThrow(() -> new RuntimeException("La variante seleccionada ya no existe (ID: " + item.getProductoVarianteId() + ")"));

            Producto producto = variante.getProducto(); // Obtenemos el padre desde la variante

            // 3. Verificar Stock
            if (variante.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException("Stock insuficiente para: " + producto.getNombre() +
                        " (" + variante.getTalla() + " - " + variante.getColor() + ")");
            }

            // 4. Crear Detalle
            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setProducto(producto);
            detalle.setProductoVariante(variante); // Guardamos la relación exacta
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setPedido(pedido);

            pedido.getDetalles().add(detalle);

            // 5. Descontar Stock
            variante.setStock(variante.getStock() - item.getCantidad());
            productoVarianteRepository.save(variante);

            // 6. Registrar en Historial
            try {
                inventarioService.registrarSalidaPorVenta(detalle);
            } catch (Exception e) {
                System.err.println("Error registrando historial: " + e.getMessage());
            }
        }

        // Calcular Total
        double total = carrito.values().stream().mapToDouble(CarritoItem::getSubtotal).sum();
        pedido.setTotal(total);

        return pedidoRepository.save(pedido);
    }

    // --- Métodos de Lectura ---
    public List<Pedido> obtenerPedidosPorCliente(Cliente cliente) {
        return pedidoRepository.findByClienteOrderByFechaCreacionDesc(cliente);
    }

    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoRepository.findAllByOrderByFechaCreacionDesc();
    }

    public List<Pedido> buscarPedidos(String clienteNombre, LocalDate fechaDesde, LocalDate fechaHasta) {
        Specification<Pedido> spec = pedidoSpecification.findByCriteria(clienteNombre, fechaDesde, fechaHasta);
        return pedidoRepository.findAll(spec);
    }
}