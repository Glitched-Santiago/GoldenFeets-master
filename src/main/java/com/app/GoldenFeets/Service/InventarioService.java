package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.DTO.InventarioEntradaDTO;
import com.app.GoldenFeets.Entity.InventarioEntrada;
import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Repository.InventarioEntradaRepository;
import com.app.GoldenFeets.Repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioEntradaRepository inventarioEntradaRepository;
    private final ProductoRepository productoRepository;

    @Transactional
    public InventarioEntrada registrarEntrada(InventarioEntradaDTO entradaDTO) {
        // 1. Buscar el producto por ID. Si no existe, lanza una excepción.
        Producto producto = productoRepository.findById(entradaDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + entradaDTO.getProductoId()));

        // 2. Crear y configurar la nueva entrada de inventario.
        InventarioEntrada nuevaEntrada = new InventarioEntrada();
        nuevaEntrada.setProducto(producto);
        nuevaEntrada.setDistribuidor(entradaDTO.getDistribuidor());
        nuevaEntrada.setCantidad(entradaDTO.getCantidad());
        nuevaEntrada.setColor(entradaDTO.getColor());
        // La fecha se establece automáticamente con @PrePersist.

        // 3. Guardar el registro de la entrada.
        inventarioEntradaRepository.save(nuevaEntrada);

        // 4. Actualizar el stock del producto.
        producto.setStock(producto.getStock() + entradaDTO.getCantidad());
        productoRepository.save(producto);

        return nuevaEntrada;
    }
}

