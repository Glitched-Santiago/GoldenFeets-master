// src/main/java/com/app/GoldenFeets/Service/InventarioService.java
package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.DTO.HistorialInventarioDTO;
import com.app.GoldenFeets.DTO.InventarioEntradaDTO;
import com.app.GoldenFeets.DTO.InventarioSalidaDTO; // <-- NUEVO DTO
import com.app.GoldenFeets.Entity.InventarioEntrada;
import com.app.GoldenFeets.Entity.InventarioSalida; // <-- NUEVA ENTIDAD
import com.app.GoldenFeets.Entity.PedidoDetalle; // <-- IMPORTANTE
import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Repository.InventarioEntradaRepository;
import com.app.GoldenFeets.Repository.InventarioSalidaRepository; // <-- INYECTAR
import com.app.GoldenFeets.Repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioEntradaRepository inventarioEntradaRepository;
    private final InventarioSalidaRepository inventarioSalidaRepository; // <-- INYECTADO
    private final ProductoRepository productoRepository;

    // --- ENTRADAS (Tu método existente, sin cambios) ---
    @Transactional
    public InventarioEntrada registrarEntrada(InventarioEntradaDTO entradaDTO) {
        // 1. Buscar el producto
        Producto producto = productoRepository.findById(entradaDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + entradaDTO.getProductoId()));

        // 2. Crear la entrada
        InventarioEntrada nuevaEntrada = new InventarioEntrada();
        nuevaEntrada.setProducto(producto);
        nuevaEntrada.setDistribuidor(entradaDTO.getDistribuidor());
        nuevaEntrada.setCantidad(entradaDTO.getCantidad());
        nuevaEntrada.setColor(entradaDTO.getColor());
        inventarioEntradaRepository.save(nuevaEntrada);

        // 3. Actualizar el stock del producto
        producto.setStock(producto.getStock() + entradaDTO.getCantidad());
        productoRepository.save(producto);

        return nuevaEntrada;
    }

    // --- NUEVO: SALIDA MANUAL ---
    @Transactional
    public InventarioSalida registrarSalidaManual(InventarioSalidaDTO salidaDTO) {
        // 1. Buscar el producto
        Producto producto = productoRepository.findById(salidaDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + salidaDTO.getProductoId()));

        // 2. Validar stock
        if (producto.getStock() < salidaDTO.getCantidad()) {
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        // 3. Crear la salida
        InventarioSalida nuevaSalida = new InventarioSalida();
        nuevaSalida.setProducto(producto);
        nuevaSalida.setCantidad(salidaDTO.getCantidad());
        nuevaSalida.setMotivo(salidaDTO.getMotivo()); // Ej: "Ajuste por pérdida"
        inventarioSalidaRepository.save(nuevaSalida);

        // 4. Actualizar el stock del producto
        producto.setStock(producto.getStock() - salidaDTO.getCantidad());
        productoRepository.save(producto);

        return nuevaSalida;
    }

    // --- NUEVO: SALIDA AUTOMÁTICA POR VENTA ---
    @Transactional
    public InventarioSalida registrarSalidaPorVenta(PedidoDetalle detalle) {
        Producto producto = detalle.getProducto();
        Integer cantidad = detalle.getCantidad();

        // 1. Validar stock (¡Importante! Esto debe hacerse ANTES de confirmar el pago)
        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
        }

        // 2. Crear la salida
        InventarioSalida nuevaSalida = new InventarioSalida();
        nuevaSalida.setProducto(producto);
        nuevaSalida.setCantidad(cantidad);
        nuevaSalida.setMotivo("Venta");
        nuevaSalida.setPedido(detalle.getPedido()); // Vinculamos al pedido
        inventarioSalidaRepository.save(nuevaSalida);

        // 3. Actualizar el stock
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);

        return nuevaSalida;
    }
    public List<HistorialInventarioDTO> getHistorialUnificado() {
        List<HistorialInventarioDTO> historial = new ArrayList<>();

        // 1. Obtener todas las entradas
        // (Asegúrate de tener 'inventarioEntradaRepository' inyectado en tu servicio)
        List<InventarioEntrada> entradas = inventarioEntradaRepository.findAll();
        for (InventarioEntrada e : entradas) {
            HistorialInventarioDTO dto = new HistorialInventarioDTO();
            dto.setFecha(e.getFechaRegistro());
            dto.setProductoNombre(e.getProducto().getNombre());
            dto.setTipo("Entrada");
            dto.setCantidad(e.getCantidad());
            dto.setDescripcion("Distribuidor: " + e.getDistribuidor());
            historial.add(dto);
        }

        // 2. Obtener todas las salidas
        // (Asegúrate de tener 'inventarioSalidaRepository' inyectado en tu servicio)
        List<InventarioSalida> salidas = inventarioSalidaRepository.findAll();
        for (InventarioSalida s : salidas) {
            HistorialInventarioDTO dto = new HistorialInventarioDTO();
            dto.setFecha(s.getFechaRegistro());
            dto.setProductoNombre(s.getProducto().getNombre());
            dto.setTipo("Salida");
            dto.setCantidad(s.getCantidad());
            dto.setDescripcion(s.getMotivo());
            if (s.getPedido() != null) {
                dto.setPedidoId(s.getPedido().getId());
                dto.setDescripcion("Venta (Pedido #" + s.getPedido().getId() + ")");
            }
            historial.add(dto);
        }

        // 3. Ordenar por fecha (más reciente primero)
        historial.sort(Comparator.comparing(HistorialInventarioDTO::getFecha).reversed());

        return historial;
    }
}