package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.*;
import com.app.GoldenFeets.Exceptions.StockInsuficienteException;
import com.app.GoldenFeets.Repository.PedidoRepository;
import com.app.GoldenFeets.Repository.ProductoRepository;
import com.app.GoldenFeets.Model.CarritoItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    // Ya no inyectamos CarritoService aquí.

    /**
     * Procesa la creación de un nuevo pedido a partir del carrito de un cliente.
     * Ahora recibe el carrito directamente como un parámetro.
     */
    @Transactional
    public Pedido crearPedido(Map<Long, CarritoItem> carrito, Cliente cliente) {
        if (carrito == null || carrito.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío, no se puede crear un pedido.");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.COMPLETADO);

        for (CarritoItem item : carrito.values()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException("No hay stock suficiente para el producto: " + producto.getNombre());
            }

            producto.setStock(producto.getStock() - item.getCantidad());

            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setPedido(pedido);

            pedido.getDetalles().add(detalle);
        }

        // Calculamos el total directamente desde el carrito
        double total = carrito.values().stream().mapToDouble(CarritoItem::getSubtotal).sum();
        pedido.setTotal(total);

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> obtenerPedidosPorCliente(Cliente cliente) {
        return pedidoRepository.findByClienteOrderByFechaCreacionDesc(cliente);
    }
}