package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.DTO.EstadisticasInventarioDTO;
import com.app.GoldenFeets.DTO.HistorialInventarioDTO;
import com.app.GoldenFeets.DTO.InventarioEntradaDTO;
import com.app.GoldenFeets.DTO.InventarioSalidaDTO;
import com.app.GoldenFeets.Entity.*;
import com.app.GoldenFeets.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioEntradaRepository inventarioEntradaRepository;
    private final InventarioSalidaRepository inventarioSalidaRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    /**
     * Registra entrada de stock y guarda la foto del stock (antes/después).
     */
    @Transactional
    public InventarioEntrada registrarEntrada(InventarioEntradaDTO entradaDTO) {
        try {
            Producto producto = productoRepository.findById(entradaDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + entradaDTO.getProductoId()));

            // Instanciamos aquí para tener la variable disponible
            InventarioEntrada nuevaEntrada = new InventarioEntrada();

            String colorInput = (entradaDTO.getColor() != null && !entradaDTO.getColor().isEmpty()) ? entradaDTO.getColor().trim() : "Único";
            String tallaInput = (entradaDTO.getTalla() != null && !entradaDTO.getTalla().isEmpty()) ? entradaDTO.getTalla().trim() : "Única";

            if (producto.getVariantes() == null) producto.setVariantes(new ArrayList<>());

            Optional<ProductoVariante> varianteExistente = producto.getVariantes().stream()
                    .filter(v -> v.getColor().equalsIgnoreCase(colorInput) && v.getTalla().equalsIgnoreCase(tallaInput))
                    .findFirst();

            ProductoVariante variante;

            if (varianteExistente.isPresent()) {
                variante = varianteExistente.get();

                // [CORRECCIÓN STOCK] Capturamos stock antes
                int stockAntes = variante.getStock();
                nuevaEntrada.setStockAnterior(stockAntes);

                variante.setStock(stockAntes + entradaDTO.getCantidad());

                // [CORRECCIÓN STOCK] Capturamos stock nuevo
                nuevaEntrada.setStockNuevo(variante.getStock());
            } else {
                variante = new ProductoVariante();
                variante.setProducto(producto);
                variante.setColor(colorInput);
                variante.setTalla(tallaInput);
                variante.setStock(entradaDTO.getCantidad());
                producto.getVariantes().add(variante);

                // [CORRECCIÓN STOCK] Si es nuevo, antes había 0
                nuevaEntrada.setStockAnterior(0);
                nuevaEntrada.setStockNuevo(entradaDTO.getCantidad());
            }

            productoRepository.save(producto);

            nuevaEntrada.setProducto(producto);
            nuevaEntrada.setDistribuidor(entradaDTO.getDistribuidor());
            nuevaEntrada.setCantidad(entradaDTO.getCantidad());
            nuevaEntrada.setColor(colorInput + " / " + tallaInput);
            nuevaEntrada.setPrecioCostoUnitario(entradaDTO.getPrecioCostoUnitario() != null ? entradaDTO.getPrecioCostoUnitario() : 0.0);

            return inventarioEntradaRepository.save(nuevaEntrada);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar entrada: " + e.getMessage());
        }
    }

    /**
     * Registra salida manual y guarda la foto del stock (antes/después).
     */
    @Transactional
    public InventarioSalida registrarSalidaManual(InventarioSalidaDTO salidaDTO) {
        try {
            Producto producto = productoRepository.findById(salidaDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            String colorTarget = (salidaDTO.getColor() != null) ? salidaDTO.getColor().trim() : "";
            String tallaTarget = (salidaDTO.getTalla() != null) ? salidaDTO.getTalla().trim() : "";

            ProductoVariante variante = producto.getVariantes().stream()
                    .filter(v -> v.getColor().equalsIgnoreCase(colorTarget) && v.getTalla().equalsIgnoreCase(tallaTarget))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Variante no encontrada: " + tallaTarget + " " + colorTarget));

            if (variante.getStock() < salidaDTO.getCantidad()) {
                throw new RuntimeException("Stock insuficiente.");
            }

            // [CORRECCIÓN STOCK] Capturamos antes
            int stockAntes = variante.getStock();

            // Restamos
            variante.setStock(stockAntes - salidaDTO.getCantidad());
            productoRepository.save(producto);

            InventarioSalida nuevaSalida = new InventarioSalida();
            nuevaSalida.setProducto(producto);
            nuevaSalida.setCantidad(salidaDTO.getCantidad());
            nuevaSalida.setMotivo(salidaDTO.getMotivo() + " [" + tallaTarget + "/" + colorTarget + "]");

            // [CORRECCIÓN STOCK] Guardamos en el historial
            nuevaSalida.setStockAnterior(stockAntes);
            nuevaSalida.setStockNuevo(variante.getStock());

            return inventarioSalidaRepository.save(nuevaSalida);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar salida: " + e.getMessage());
        }
    }

    /**
     * Registra salida por Venta.
     * OJO: El stock YA FUE RESTADO en PedidoService. Aquí solo registramos la foto.
     */
    @Transactional
    public InventarioSalida registrarSalidaPorVenta(PedidoDetalle detalle) {
        Producto producto = detalle.getProducto();
        ProductoVariante variante = detalle.getProductoVariante(); // Variante ya actualizada en PedidoService

        InventarioSalida nuevaSalida = new InventarioSalida();
        nuevaSalida.setProducto(producto);
        nuevaSalida.setCantidad(detalle.getCantidad());
        nuevaSalida.setMotivo("Venta Web");
        nuevaSalida.setPedido(detalle.getPedido());

        // [CORRECCIÓN STOCK]
        // Como PedidoService ya restó, el stock actual de la variante es el "Nuevo".
        // El "Anterior" era: Actual + lo que se vendió.
        int stockActual = variante.getStock();
        nuevaSalida.setStockNuevo(stockActual);
        nuevaSalida.setStockAnterior(stockActual + detalle.getCantidad());

        return inventarioSalidaRepository.save(nuevaSalida);
    }

    /**
     * Mapea los datos al DTO para verlos en el HTML.
     */
    @Transactional(readOnly = true)
    public List<HistorialInventarioDTO> getHistorialUnificado(
            LocalDate fechaDesde, LocalDate fechaHasta, String productoNombre, String tipo
    ) {
        List<HistorialInventarioDTO> historial = new ArrayList<>();

        // 1. Procesar Entradas
        List<InventarioEntrada> entradas = inventarioEntradaRepository.findAllWithProducto();
        for (InventarioEntrada e : entradas) {
            if (e.getFechaRegistro() == null) continue;

            HistorialInventarioDTO dto = new HistorialInventarioDTO();
            dto.setFecha(e.getFechaRegistro());
            dto.setProductoNombre(e.getProducto() != null ? e.getProducto().getNombre() : "Eliminado");
            dto.setTipo("Entrada");
            dto.setCantidad(e.getCantidad());
            dto.setDescripcion("Prov: " + (e.getDistribuidor() != null ? e.getDistribuidor() : "N/A") + " (" + e.getColor() + ")");

            // [CORRECCIÓN MAPEO] Pasar los stocks al DTO
            dto.setStockAnterior(e.getStockAnterior());
            dto.setStockNuevo(e.getStockNuevo());

            Double costo = (e.getPrecioCostoUnitario() != null) ? e.getPrecioCostoUnitario() : 0.0;
            dto.setPrecioUnitario(costo);
            dto.setValorMonetario(e.getCantidad() * costo);

            historial.add(dto);
        }

        // 2. Procesar Salidas
        List<InventarioSalida> salidas = inventarioSalidaRepository.findAllWithPedidoAndDetalles();
        for (InventarioSalida s : salidas) {
            if (s.getFechaRegistro() == null) continue;

            HistorialInventarioDTO dto = new HistorialInventarioDTO();
            dto.setFecha(s.getFechaRegistro());
            dto.setProductoNombre(s.getProducto() != null ? s.getProducto().getNombre() : "Eliminado");
            dto.setTipo("Salida");
            dto.setCantidad(s.getCantidad());

            // [CORRECCIÓN MAPEO] Pasar los stocks al DTO
            dto.setStockAnterior(s.getStockAnterior());
            dto.setStockNuevo(s.getStockNuevo());

            if (s.getPedido() != null) {
                dto.setPedidoId(s.getPedido().getId());
                dto.setDescripcion("Venta #" + s.getPedido().getId());

                double precioVenta = 0.0;
                try {
                    if (s.getPedido().getDetalles() != null) {
                        precioVenta = s.getPedido().getDetalles().stream()
                                .filter(d -> d.getProducto().getId().equals(s.getProducto().getId()))
                                .findFirst().map(PedidoDetalle::getPrecioUnitario).orElse(0.0);
                    }
                } catch (Exception ignored) {}
                dto.setPrecioUnitario(precioVenta);
                dto.setValorMonetario(s.getCantidad() * precioVenta);
            } else {
                dto.setDescripcion(s.getMotivo());
                dto.setPrecioUnitario(0.0);
                dto.setValorMonetario(0.0);
            }
            historial.add(dto);
        }

        // 3. Filtros
        Stream<HistorialInventarioDTO> stream = historial.stream();
        if (fechaDesde != null) stream = stream.filter(h -> !h.getFecha().toLocalDate().isBefore(fechaDesde));
        if (fechaHasta != null) stream = stream.filter(h -> !h.getFecha().toLocalDate().isAfter(fechaHasta));
        if (productoNombre != null && !productoNombre.isBlank()) stream = stream.filter(h -> h.getProductoNombre().toLowerCase().contains(productoNombre.toLowerCase()));
        if (tipo != null && !tipo.isBlank()) stream = stream.filter(h -> h.getTipo().equalsIgnoreCase(tipo));

        return stream.sorted(Comparator.comparing(HistorialInventarioDTO::getFecha).reversed()).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EstadisticasInventarioDTO getEstadisticasInventario(LocalDate fechaDesde, LocalDate fechaHasta) {
        EstadisticasInventarioDTO stats = new EstadisticasInventarioDTO();

        List<InventarioEntrada> entradas = inventarioEntradaRepository.findAllWithProducto();
        double totalGastado = entradas.stream()
                .filter(e -> e.getFechaRegistro() != null)
                .filter(e -> fechaDesde == null || !e.getFechaRegistro().toLocalDate().isBefore(fechaDesde))
                .filter(e -> fechaHasta == null || !e.getFechaRegistro().toLocalDate().isAfter(fechaHasta))
                .mapToDouble(e -> (e.getCantidad() * (e.getPrecioCostoUnitario() != null ? e.getPrecioCostoUnitario() : 0.0)))
                .sum();

        List<Pedido> pedidos = pedidoRepository.findAllByEstado(EstadoPedido.PAGADO);
        double totalIngresado = pedidos.stream()
                .filter(p -> p.getFechaCreacion() != null)
                .filter(p -> fechaDesde == null || !p.getFechaCreacion().toLocalDate().isBefore(fechaDesde))
                .filter(p -> fechaHasta == null || !p.getFechaCreacion().toLocalDate().isAfter(fechaHasta))
                .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
                .sum();

        stats.setTotalGastado(totalGastado);
        stats.setTotalIngresado(totalIngresado);
        stats.setBeneficioNeto(totalIngresado - totalGastado);

        return stats;
    }
}