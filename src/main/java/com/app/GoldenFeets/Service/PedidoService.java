package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.EstadoPedido;
import com.app.GoldenFeets.Entity.Pedido;
import com.app.GoldenFeets.Entity.PedidoDetalle;
import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Exceptions.StockInsuficienteException;
import com.app.GoldenFeets.Model.CarritoItem;
import com.app.GoldenFeets.Repository.PedidoRepository;
import com.app.GoldenFeets.Repository.ProductoRepository;
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

    /**
     * Procesa la creación de un nuevo pedido a partir del carrito de un cliente.
     * Este método es transaccional, lo que significa que si algo falla (ej. falta de stock),
     * todos los cambios en la base de datos se revierten.
     */
    @Transactional
    public Pedido crearPedido(Map<Long, CarritoItem> carrito, Cliente cliente) {
        if (carrito == null || carrito.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío, no se puede crear un pedido.");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.COMPLETADO); // O el estado inicial que prefieras

        for (CarritoItem item : carrito.values()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException("No hay stock suficiente para el producto: " + producto.getNombre());
            }

            // Descontamos el stock del producto
            producto.setStock(producto.getStock() - item.getCantidad());

            // Creamos el detalle del pedido
            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio()); // Guarda el precio al momento de la compra
            detalle.setPedido(pedido);

            pedido.getDetalles().add(detalle);
        }

        // Calculamos el total del pedido
        double total = carrito.values().stream().mapToDouble(CarritoItem::getSubtotal).sum();
        pedido.setTotal(total);

        return pedidoRepository.save(pedido);
    }

    /**
     * Devuelve el historial de pedidos para un cliente específico.
     * Usado en el panel del cliente.
     */
    public List<Pedido> obtenerPedidosPorCliente(Cliente cliente) {
        return pedidoRepository.findByClienteOrderByFechaCreacionDesc(cliente);
    }

    /**
     * Devuelve una lista de todos los pedidos realizados en el sistema.
     * Usado en el panel del administrador.
     */
    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoRepository.findAllByOrderByFechaCreacionDesc();
    }

    /**
     * Busca pedidos basado en múltiples criterios (nombre de cliente y rango de fechas).
     * Usado para los filtros en el panel del administrador.
     */
    public List<Pedido> buscarPedidos(String clienteNombre, LocalDate fechaDesde, LocalDate fechaHasta) {
        Specification<Pedido> spec = pedidoSpecification.findByCriteria(clienteNombre, fechaDesde, fechaHasta);
        return pedidoRepository.findAll(spec);
    }
}