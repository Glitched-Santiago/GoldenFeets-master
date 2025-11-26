package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.*;
import com.app.GoldenFeets.Exceptions.StockInsuficienteException;
import com.app.GoldenFeets.Model.CarritoItem;
import com.app.GoldenFeets.Repository.PedidoRepository;
import com.app.GoldenFeets.Repository.ProductoRepository;
// [NUEVO] Importamos el repositorio y la entidad de variantes
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
    private final PedidoSpecification pedidoSpecification;

    // [NUEVO] Inyectamos el repositorio para buscar tallas y colores
    private final ProductoVarianteRepository productoVarianteRepository;

    private final InventarioService inventarioService;

    /**
     * Procesa la creación de un nuevo pedido a partir del carrito de un cliente.
     */
    @Transactional
    public Pedido crearPedido(Map<Long, CarritoItem> carrito, Cliente cliente) {
        if (carrito == null || carrito.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío, no se puede crear un pedido.");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.PAGADO);

        for (CarritoItem item : carrito.values()) {
            // 1. Buscamos el Producto padre
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + item.getProductoId()));

            // 2. [NUEVO] Buscamos la Variante específica (Talla y Color)
            // IMPORTANTE: Asegúrate de que tu clase CarritoItem tenga los métodos getTalla() y getColor()
            ProductoVariante variante = productoVarianteRepository
                    .findByProductoAndTallaAndColor(producto, item.getTalla(), item.getColor())
                    .orElseThrow(() -> new RuntimeException("No disponible: " + producto.getNombre() + " Talla: " + item.getTalla()));

            // 3. [CORREGIDO] Verificación de stock en la VARIANTE (ya no en producto.getStock())
            if (variante.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException("No hay stock suficiente para: " + producto.getNombre() + " (" + item.getTalla() + ")");
            }

            // 4. Creamos el detalle del pedido
            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setProducto(producto);

            // [NUEVO] Guardamos qué variante exacta se compró
            detalle.setProductoVariante(variante);

            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setPedido(pedido);

            pedido.getDetalles().add(detalle);

            // 5. [LÓGICA ACTUALIZADA] Descontar stock directamente de la variante
            variante.setStock(variante.getStock() - item.getCantidad());
            productoVarianteRepository.save(variante);

            // Registro en historial (opcional, si tu inventarioService lo requiere)
            try {
                inventarioService.registrarSalidaPorVenta(detalle);
            } catch (Exception e) {
                // Logueamos el error pero no detenemos la venta si el stock ya se descontó arriba
                System.err.println("Advertencia al registrar historial: " + e.getMessage());
            }
        }

        // Calculamos el total
        double total = carrito.values().stream().mapToDouble(CarritoItem::getSubtotal).sum();
        pedido.setTotal(total);

        return pedidoRepository.save(pedido);
    }

    // --- MÉTODOS DE CONSULTA (SIN CAMBIOS) ---

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