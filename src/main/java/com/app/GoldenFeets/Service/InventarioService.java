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
     * Registra una entrada de inventario.
     * Si la variante (Talla/Color) ya existe, suma el stock.
     * Si no existe, crea una nueva variante asociada al producto.
     */
    @Transactional
    public InventarioEntrada registrarEntrada(InventarioEntradaDTO entradaDTO) {
        System.out.println("--- DEBUG: Iniciando registrarEntrada ---");
        try {
            // 1. Buscar el producto padre
            Producto producto = productoRepository.findById(entradaDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + entradaDTO.getProductoId()));

            // 2. Normalizar entradas (evitar duplicados por mayúsculas/minúsculas)
            String colorInput = (entradaDTO.getColor() != null && !entradaDTO.getColor().isEmpty())
                    ? entradaDTO.getColor().trim() : "Único";
            String tallaInput = (entradaDTO.getTalla() != null && !entradaDTO.getTalla().isEmpty())
                    ? entradaDTO.getTalla().trim() : "Única";

            // 3. Buscar si la variante ya existe
            Optional<ProductoVariante> varianteExistente = producto.getVariantes().stream()
                    .filter(v -> v.getColor().equalsIgnoreCase(colorInput) && v.getTalla().equalsIgnoreCase(tallaInput))
                    .findFirst();

            ProductoVariante variante;

            if (varianteExistente.isPresent()) {
                // Opción A: Actualizar existente
                variante = varianteExistente.get();
                variante.setStock(variante.getStock() + entradaDTO.getCantidad());
                System.out.println("DEBUG: Variante encontrada. Nuevo stock: " + variante.getStock());
            } else {
                // Opción B: Crear nueva variante
                variante = new ProductoVariante();
                variante.setProducto(producto);
                variante.setColor(colorInput);
                variante.setTalla(tallaInput);
                variante.setStock(entradaDTO.getCantidad());
                producto.getVariantes().add(variante); // Agregamos a la lista del padre
                System.out.println("DEBUG: Nueva variante creada: " + tallaInput + " - " + colorInput);
            }

            // 4. Guardar cambios en Producto (Cascade guardará la variante)
            productoRepository.save(producto);

            // 5. Crear Registro Histórico de Entrada
            InventarioEntrada nuevaEntrada = new InventarioEntrada();
            nuevaEntrada.setProducto(producto);
            nuevaEntrada.setDistribuidor(entradaDTO.getDistribuidor());
            nuevaEntrada.setCantidad(entradaDTO.getCantidad());
            // Guardamos el detalle de la variante en el campo 'color' de la tabla histórica o similar
            // Para mantener compatibilidad con tu tabla actual de entradas que tiene campo 'color':
            nuevaEntrada.setColor(colorInput + " / " + tallaInput);

            if (entradaDTO.getPrecioCostoUnitario() != null) {
                nuevaEntrada.setPrecioCostoUnitario(entradaDTO.getPrecioCostoUnitario());
            } else {
                nuevaEntrada.setPrecioCostoUnitario(0.0);
            }

            return inventarioEntradaRepository.save(nuevaEntrada);

        } catch (Exception e) {
            System.err.println("--- ERROR en registrarEntrada ---");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Registra una salida manual (pérdida, daño, uso interno).
     * Requiere especificar Talla y Color en el DTO para descontar de la variante correcta.
     */
    @Transactional
    public InventarioSalida registrarSalidaManual(InventarioSalidaDTO salidaDTO) {
        System.out.println("--- DEBUG: Iniciando registrarSalidaManual ---");
        try {
            // 1. Buscar Producto
            Producto producto = productoRepository.findById(salidaDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // 2. Identificar Variante a descontar
            // Asumimos que el DTO viene con Talla y Color desde el formulario
            String colorTarget = (salidaDTO.getColor() != null) ? salidaDTO.getColor().trim() : "";
            String tallaTarget = (salidaDTO.getTalla() != null) ? salidaDTO.getTalla().trim() : "";

            ProductoVariante variante = producto.getVariantes().stream()
                    .filter(v -> v.getColor().equalsIgnoreCase(colorTarget) && v.getTalla().equalsIgnoreCase(tallaTarget))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró la variante " + tallaTarget + " - " + colorTarget + " para este producto."));

            // 3. Validar Stock
            if (variante.getStock() < salidaDTO.getCantidad()) {
                throw new RuntimeException("Stock insuficiente. Disponible: " + variante.getStock() + ", Solicitado: " + salidaDTO.getCantidad());
            }

            // 4. Descontar y Guardar
            variante.setStock(variante.getStock() - salidaDTO.getCantidad());
            productoRepository.save(producto);

            // 5. Registro Histórico
            InventarioSalida nuevaSalida = new InventarioSalida();
            nuevaSalida.setProducto(producto);
            nuevaSalida.setCantidad(salidaDTO.getCantidad());
            nuevaSalida.setMotivo(salidaDTO.getMotivo() + " [" + tallaTarget + "/" + colorTarget + "]");

            return inventarioSalidaRepository.save(nuevaSalida);

        } catch (Exception e) {
            System.err.println("--- ERROR en registrarSalidaManual ---");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Registra salida automática por venta (Checkout).
     * Usa el objeto PedidoDetalle que ya tiene vinculada la ProductoVariante.
     */
    @Transactional
    public InventarioSalida registrarSalidaPorVenta(PedidoDetalle detalle) {
        Producto producto = detalle.getProducto();
        ProductoVariante variante = detalle.getProductoVariante();

        // Validación de seguridad (aunque PedidoService ya debió validar)
        if (variante == null) {
            throw new RuntimeException("Error crítico: El detalle del pedido no tiene variante asignada.");
        }

        Integer cantidad = detalle.getCantidad();
        if (variante.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para confirmar venta: " + producto.getNombre() + " (" + variante.getTalla() + ")");
        }

        // Descontar Stock
        variante.setStock(variante.getStock() - cantidad);
        productoRepository.save(producto);

        // Registro Histórico
        InventarioSalida nuevaSalida = new InventarioSalida();
        nuevaSalida.setProducto(producto);
        nuevaSalida.setCantidad(cantidad);
        nuevaSalida.setMotivo("Venta Web");
        nuevaSalida.setPedido(detalle.getPedido());

        return inventarioSalidaRepository.save(nuevaSalida);
    }

    /**
     * Obtiene historial combinado de entradas y salidas.
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
            dto.setProductoNombre(e.getProducto() != null ? e.getProducto().getNombre() : "Producto Eliminado");
            dto.setTipo("Entrada");
            dto.setCantidad(e.getCantidad());
            dto.setDescripcion("Prov: " + (e.getDistribuidor() != null ? e.getDistribuidor() : "N/A") +
                    (e.getColor() != null ? " (" + e.getColor() + ")" : ""));

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
            dto.setProductoNombre(s.getProducto() != null ? s.getProducto().getNombre() : "Producto Eliminado");
            dto.setTipo("Salida");
            dto.setCantidad(s.getCantidad());

            if (s.getPedido() != null) {
                // Es una venta
                dto.setPedidoId(s.getPedido().getId());
                dto.setDescripcion("Venta #" + s.getPedido().getId());

                // Intentar obtener precio de venta del detalle
                double precioVenta = 0.0;
                if (s.getPedido().getDetalles() != null) {
                    precioVenta = s.getPedido().getDetalles().stream()
                            .filter(d -> d.getProducto().getId().equals(s.getProducto().getId()))
                            .findFirst()
                            .map(PedidoDetalle::getPrecioUnitario)
                            .orElse(0.0);
                }
                dto.setPrecioUnitario(precioVenta);
                dto.setValorMonetario(s.getCantidad() * precioVenta);
            } else {
                // Es manual
                dto.setDescripcion(s.getMotivo());
                dto.setPrecioUnitario(0.0);
                dto.setValorMonetario(0.0);
            }
            historial.add(dto);
        }

        // 3. Filtrar y Ordenar
        Stream<HistorialInventarioDTO> stream = historial.stream();

        if (fechaDesde != null) {
            stream = stream.filter(h -> !h.getFecha().toLocalDate().isBefore(fechaDesde));
        }
        if (fechaHasta != null) {
            stream = stream.filter(h -> !h.getFecha().toLocalDate().isAfter(fechaHasta));
        }
        if (productoNombre != null && !productoNombre.isBlank()) {
            String term = productoNombre.toLowerCase();
            stream = stream.filter(h -> h.getProductoNombre().toLowerCase().contains(term));
        }
        if (tipo != null && !tipo.isBlank()) {
            stream = stream.filter(h -> h.getTipo().equalsIgnoreCase(tipo));
        }

        return stream
                .sorted(Comparator.comparing(HistorialInventarioDTO::getFecha).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Calcula estadísticas financieras básicas.
     */
    @Transactional(readOnly = true)
    public EstadisticasInventarioDTO getEstadisticasInventario(LocalDate fechaDesde, LocalDate fechaHasta) {
        EstadisticasInventarioDTO stats = new EstadisticasInventarioDTO();

        // Calcular Gastos (Entradas)
        List<InventarioEntrada> entradas = inventarioEntradaRepository.findAllWithProducto();
        double totalGastado = entradas.stream()
                .filter(e -> e.getFechaRegistro() != null)
                .filter(e -> fechaDesde == null || !e.getFechaRegistro().toLocalDate().isBefore(fechaDesde))
                .filter(e -> fechaHasta == null || !e.getFechaRegistro().toLocalDate().isAfter(fechaHasta))
                .mapToDouble(e -> (e.getCantidad() * (e.getPrecioCostoUnitario() != null ? e.getPrecioCostoUnitario() : 0.0)))
                .sum();

        // Calcular Ingresos (Pedidos Pagados)
        List<Pedido> pedidos = pedidoRepository.findAllByEstado(EstadoPedido.PAGADO); // Asegúrate que tu Enum tenga PAGADO
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