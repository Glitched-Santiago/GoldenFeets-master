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
import java.time.LocalDate;
import java.util.stream.Stream;

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
                System.out.println("DEBUG (Entrada): Pre    cio de costo DTO es NULL. Se usará el default 0.0");
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
    @Transactional(readOnly = true)
    public List<HistorialInventarioDTO> getHistorialUnificado(
            LocalDate fechaDesde, LocalDate fechaHasta, String productoNombre, String tipo
    ) {
        System.out.println("\n--- DEBUG: Iniciando getHistorialUnificado (Arreglo ImmutableList) ---");
        List<HistorialInventarioDTO> historial = new ArrayList<>();

        // 1. Obtener entradas (CON LA NUEVA CONSULTA)
        List<InventarioEntrada> entradas = inventarioEntradaRepository.findAllWithProducto();
        System.out.println("DEBUG (Lectura): Entradas encontradas (con fetch): " + entradas.size());

        for (InventarioEntrada e : entradas) {
            // ... (Tu lógica de mapeo de entrada se queda igual)
            if (e == null || e.getFechaRegistro() == null) continue;
            HistorialInventarioDTO dto = new HistorialInventarioDTO();
            dto.setFecha(e.getFechaRegistro());
            dto.setProductoNombre(e.getProducto() != null ? e.getProducto().getNombre() : "Producto Desconocido");
            dto.setTipo("Entrada");
            dto.setCantidad(e.getCantidad());
            dto.setDescripcion("Distribuidor: " + (e.getDistribuidor() != null ? e.getDistribuidor() : "N/A"));
            Double costoUnitario = (e.getPrecioCostoUnitario() != null) ? e.getPrecioCostoUnitario() : 0.0;
            dto.setPrecioUnitario(costoUnitario);
            dto.setValorMonetario((e.getCantidad() != null ? e.getCantidad() : 0) * costoUnitario);
            historial.add(dto);
        }

        // 2. Obtener salidas (CON LA NUEVA CONSULTA)
        List<InventarioSalida> salidas = inventarioSalidaRepository.findAllWithPedidoAndDetalles();
        System.out.println("DEBUG (Lectura): Salidas encontradas (con fetch): " + salidas.size());

        for (InventarioSalida s : salidas) {
            // ... (Tu lógica de mapeo de salida se queda igual)
            if (s == null || s.getFechaRegistro() == null) continue;
            HistorialInventarioDTO dto = new HistorialInventarioDTO();
            dto.setFecha(s.getFechaRegistro());
            dto.setProductoNombre(s.getProducto() != null ? s.getProducto().getNombre() : "Producto Desconocido");
            dto.setTipo("Salida");
            dto.setCantidad(s.getCantidad());

            if (s.getPedido() != null) {
                Pedido pedido = s.getPedido(); // ¡Esto ya está cargado!
                dto.setPedidoId(pedido.getId());
                dto.setDescripcion("Venta (Pedido #" + pedido.getId() + ")");

                if (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
                    PedidoDetalle detalle = pedido.getDetalles().stream()
                            .filter(d -> d != null && d.getProducto() != null && s.getProducto() != null && d.getProducto().getId().equals(s.getProducto().getId()))
                            .findFirst()
                            .orElse(null);

                    if (detalle != null && detalle.getPrecioUnitario() != null) {
                        dto.setPrecioUnitario(detalle.getPrecioUnitario());
                        dto.setValorMonetario((s.getCantidad() != null ? s.getCantidad() : 0) * detalle.getPrecioUnitario());
                    } else {
                        dto.setPrecioUnitario(0.0);
                        dto.setValorMonetario(0.0);
                    }
                } else {
                    dto.setPrecioUnitario(0.0);
                    dto.setValorMonetario(0.0);
                }
            } else {
                dto.setDescripcion(s.getMotivo() != null ? s.getMotivo() : "Salida Manual/Ajuste");
                dto.setPrecioUnitario(0.0);
                dto.setValorMonetario(0.0);
            }
            historial.add(dto);
        }

        // --- FILTRADO ---
        Stream<HistorialInventarioDTO> stream = historial.stream();

        if (fechaDesde != null) {
            stream = stream.filter(h -> h.getFecha() != null && !h.getFecha().toLocalDate().isBefore(fechaDesde));
        }
        if (fechaHasta != null) {
            stream = stream.filter(h -> h.getFecha() != null && !h.getFecha().toLocalDate().isAfter(fechaHasta));
        }
        if (productoNombre != null && !productoNombre.trim().isEmpty()) {
            String lowerProducto = productoNombre.trim().toLowerCase();
            stream = stream.filter(h -> h.getProductoNombre() != null && h.getProductoNombre().toLowerCase().contains(lowerProducto));
        }
        if (tipo != null && !tipo.isEmpty()) {
            stream = stream.filter(h -> h.getTipo() != null && h.getTipo().equalsIgnoreCase(tipo));
        }

        // --- ¡AQUÍ ESTÁ EL ARREGLO, MI PANA! ---

        // 3. Ordenar DENTRO del stream
        List<HistorialInventarioDTO> historialFiltrado = stream
                .sorted(Comparator.comparing(HistorialInventarioDTO::getFecha).reversed()) // <-- ¡ORDENAMOS AQUÍ!
                .toList(); // Y ahora sí lo volvemos lista

        // ¡Y BORRAMOS LA LÍNEA QUE SE REVENTABA!
        // historialFiltrado.sort(Comparator.comparing(HistorialInventarioDTO::getFecha).reversed());  <-- ¡ESTA SE VA!

        System.out.println("DEBUG (Lectura): Total items en historial (filtrado y ordenado): " + historialFiltrado.size());

        return historialFiltrado;
    }

    @Transactional(readOnly = true)
    public EstadisticasInventarioDTO getEstadisticasInventario(
            LocalDate fechaDesde, LocalDate fechaHasta
    ) {
        System.out.println("\n--- DEBUG: Iniciando getEstadisticasInventario (CON FETCH JOINS) ---");
        EstadisticasInventarioDTO stats = new EstadisticasInventarioDTO();

        // --- CÁLCULO SEGURO DE GASTADO (CON NUEVA CONSULTA) ---
        Stream<InventarioEntrada> entradasStream = inventarioEntradaRepository.findAllWithProducto().stream();

        if (fechaDesde != null) {
            entradasStream = entradasStream.filter(e -> e.getFechaRegistro() != null && !e.getFechaRegistro().toLocalDate().isBefore(fechaDesde));
        }
        if (fechaHasta != null) {
            entradasStream = entradasStream.filter(e -> e.getFechaRegistro() != null && !e.getFechaRegistro().toLocalDate().isAfter(fechaHasta));
        }
        double totalGastado = entradasStream.mapToDouble(e -> (e.getCantidad() != null && e.getPrecioCostoUnitario() != null) ? e.getCantidad() * e.getPrecioCostoUnitario() : 0.0).sum();
        stats.setTotalGastado(totalGastado);
        System.out.println("DEBUG (Stats): Total Gastado (Filtrado): " + totalGastado);

        // --- CÁLCULO SEGURO DE INGRESADO (CON NUEVA CONSULTA) ---
        // Usamos el EstadoPedido.PAGADO, ¡asegúrate que exista en tu Enum!
        List<Pedido> pedidos = pedidoRepository.findAllByEstado(EstadoPedido.PAGADO);
        Stream<Pedido> pedidosStream = pedidos.stream();

        if (fechaDesde != null) {
            pedidosStream = pedidosStream.filter(p -> p.getFechaCreacion() != null && !p.getFechaCreacion().toLocalDate().isBefore(fechaDesde));
        }
        if (fechaHasta != null) {
            pedidosStream = pedidosStream.filter(p -> p.getFechaCreacion() != null && !p.getFechaCreacion().toLocalDate().isAfter(fechaHasta));
        }
        double totalIngresado = pedidosStream.mapToDouble(p -> (p.getTotal() != null) ? p.getTotal() : 0.0).sum();
        stats.setTotalIngresado(totalIngresado);
        System.out.println("DEBUG (Stats): Total Ingresado (Filtrado): " + totalIngresado);

        return stats;
    }
}