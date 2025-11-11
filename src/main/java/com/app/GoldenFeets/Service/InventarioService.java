// src/main/java/com/app/GoldenFeets/Service/InventarioService.java
package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.DTO.EstadisticasInventarioDTO;
import com.app.GoldenFeets.DTO.HistorialInventarioDTO;
import com.app.GoldenFeets.DTO.InventarioEntradaDTO;
import com.app.GoldenFeets.DTO.InventarioSalidaDTO;
import com.app.GoldenFeets.Entity.*;
import com.app.GoldenFeets.Repository.InventarioEntradaRepository;
import com.app.GoldenFeets.Repository.InventarioSalidaRepository;
import com.app.GoldenFeets.Repository.PedidoRepository;
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
    private final InventarioSalidaRepository inventarioSalidaRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional
    public InventarioEntrada registrarEntrada(InventarioEntradaDTO entradaDTO) {
        // --- INICIO DEPURACIÓN ---
        System.out.println("\n--- DEBUG: Iniciando registrarEntrada ---");
        try {
            // 1. Buscar el producto
            System.out.println("DEBUG (Entrada): Buscando producto ID: " + entradaDTO.getProductoId());
            Producto producto = productoRepository.findById(entradaDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + entradaDTO.getProductoId()));
            System.out.println("DEBUG (Entrada): Producto encontrado: " + producto.getNombre());

            // 2. Crear la entrada
            InventarioEntrada nuevaEntrada = new InventarioEntrada();
            nuevaEntrada.setProducto(producto);
            nuevaEntrada.setDistribuidor(entradaDTO.getDistribuidor());
            nuevaEntrada.setCantidad(entradaDTO.getCantidad());
            nuevaEntrada.setColor(entradaDTO.getColor());

            if (entradaDTO.getPrecioCostoUnitario() != null) {
                nuevaEntrada.setPrecioCostoUnitario(entradaDTO.getPrecioCostoUnitario());
                System.out.println("DEBUG (Entrada): Precio de costo DTO: " + entradaDTO.getPrecioCostoUnitario());
            } else {
                System.out.println("DEBUG (Entrada): Precio de costo DTO es NULL. Se usará el default 0.0");
            }

            // 3. Guardar el registro de la entrada.
            System.out.println("DEBUG (Entrada): Guardando nuevaEntrada...");
            inventarioEntradaRepository.save(nuevaEntrada);
            System.out.println("DEBUG (Entrada): ¡nuevaEntrada GUARDADA! (ID: " + nuevaEntrada.getId() + ")");

            // 4. Actualizar el stock del producto.
            System.out.println("DEBUG (Entrada): Actualizando stock del producto...");
            producto.setStock(producto.getStock() + entradaDTO.getCantidad()); // La corrección del typo
            productoRepository.save(producto);
            System.out.println("DEBUG (Entrada): ¡Stock del producto ACTUALIZADO!");

            System.out.println("--- DEBUG: registrarEntrada FINALIZADO CON ÉXITO ---");
            return nuevaEntrada;

        } catch (Exception e) {
            // --- ¡CRÍTICO! Imprime el error ANTES de que el @Transactional lo revierta ---
            System.err.println("--- ERROR FATAL en registrarEntrada ---");
            e.printStackTrace(); // Imprime todo el stack trace
            System.err.println("--------------------------------------");
            throw e; // Vuelve a lanzar el error para que el rollback ocurra
        }
    }

    @Transactional
    public InventarioSalida registrarSalidaManual(InventarioSalidaDTO salidaDTO) {
        // --- INICIO DEPURACIÓN ---
        System.out.println("\n--- DEBUG: Iniciando registrarSalidaManual ---");
        try {
            // 1. Buscar el producto
            System.out.println("DEBUG (Salida): Buscando producto ID: " + salidaDTO.getProductoId());
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
            nuevaSalida.setMotivo(salidaDTO.getMotivo());

            System.out.println("DEBUG (Salida): Guardando nuevaSalida...");
            inventarioSalidaRepository.save(nuevaSalida);
            System.out.println("DEBUG (Salida): ¡nuevaSalida GUARDADA! (ID: " + nuevaSalida.getId() + ")");

            // 4. Actualizar el stock del producto.
            System.out.println("DEBUG (Salida): Actualizando stock del producto...");
            producto.setStock(producto.getStock() - salidaDTO.getCantidad());
            productoRepository.save(producto);
            System.out.println("DEBUG (Salida): ¡Stock del producto ACTUALIZADO!");

            System.out.println("--- DEBUG: registrarSalidaManual FINALIZADO CON ÉXITO ---");
            return nuevaSalida;

        } catch (Exception e) {
            // --- ¡CRÍTICO! Imprime el error ANTES de que el @Transactional lo revierta ---
            System.err.println("--- ERROR FATAL en registrarSalidaManual ---");
            e.printStackTrace(); // Imprime todo el stack trace
            System.err.println("-----------------------------------------");
            throw e; // Vuelve a lanzar el error
        }
    }

    // (Dejamos este método igual, ya estaba bien)
    @Transactional
    public InventarioSalida registrarSalidaPorVenta(PedidoDetalle detalle) {
        Producto producto = detalle.getProducto();
        Integer cantidad = detalle.getCantidad();
        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
        }
        InventarioSalida nuevaSalida = new InventarioSalida();
        nuevaSalida.setProducto(producto);
        nuevaSalida.setCantidad(cantidad);
        nuevaSalida.setMotivo("Venta");
        nuevaSalida.setPedido(detalle.getPedido());
        inventarioSalidaRepository.save(nuevaSalida);
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
        return nuevaSalida;
    }

    // --- DEPURACIÓN EN LECTURA ---

    @Transactional(readOnly = true)
    public List<HistorialInventarioDTO> getHistorialUnificado() {
        System.out.println("\n--- DEBUG: Iniciando getHistorialUnificado (Lectura) ---");
        List<HistorialInventarioDTO> historial = new ArrayList<>();

        // 1. Obtener todas las entradas
        List<InventarioEntrada> entradas = inventarioEntradaRepository.findAll();
        System.out.println("DEBUG (Lectura): Entradas encontradas en BBDD: " + entradas.size());

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
        List<InventarioSalida> salidas = inventarioSalidaRepository.findAll();
        System.out.println("DEBUG (Lectura): Salidas encontradas en BBDD: " + salidas.size());

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
        System.out.println("DEBUG (Lectura): Total items en historial: " + historial.size());
        System.out.println("--- DEBUG: getHistorialUnificado FINALIZADO ---");
        return historial;
    }

    @Transactional(readOnly = true)
    public EstadisticasInventarioDTO getEstadisticasInventario() {
        System.out.println("\n--- DEBUG: Iniciando getEstadisticasInventario ---");
        EstadisticasInventarioDTO stats = new EstadisticasInventarioDTO();

        double totalGastado = inventarioEntradaRepository.findAll().stream()
                .mapToDouble(InventarioEntrada::getCostoTotalEntrada)
                .sum();
        stats.setTotalGastado(totalGastado);
        System.out.println("DEBUG (Stats): Total Gastado: " + totalGastado);

        double totalIngresado = pedidoRepository.findAll().stream()
                .filter(pedido -> pedido.getEstado() == EstadoPedido.PAGADO) // Ajusta tu enum
                .mapToDouble(Pedido::getTotal)
                .sum();
        stats.setTotalIngresado(totalIngresado);
        System.out.println("DEBUG (Stats): Total Ingresado: " + totalIngresado);
        System.out.println("--- DEBUG: getEstadisticasInventario FINALIZADO ---");

        return stats;
    }
}