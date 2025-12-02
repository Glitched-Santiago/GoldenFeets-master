package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Entity.ProductoVariante;
import com.app.GoldenFeets.Repository.ProductoRepository;
import com.app.GoldenFeets.Repository.ProductoVarianteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoVarianteRepository productoVarianteRepository;
    private final InventarioService inventarioService;

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerProductosDisponibles() {
        return productoRepository.findAllConStock();
    }

    public List<Producto> search(String keyword, Long categoriaId, Double precioMin, Double precioMax, String talla, String color) {
        return productoRepository.searchByFilters(keyword, categoriaId, precioMin, precioMax, talla, color);
    }

    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }

    public List<Producto> encontrarProductosAleatorios(int limite) {
        return productoRepository.findRandomProductos(limite);
    }

    @Transactional
    public void crearVarianteManual(Long productoId, String talla, String color, String imagenUrl) {

        // 1. NORMALIZAR TEXTO (Ej: " rojo " -> "Rojo")
        String colorNorm = (color != null && !color.trim().isEmpty())
                ? normalizarTexto(color)
                : "Único";

        String tallaNorm = (talla != null) ? talla.trim().toUpperCase() : "ÚNICA";

        // 2. Buscar Producto
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 3. Buscar si ya existe la variante específica (Producto + Talla + Color)
        Optional<ProductoVariante> varianteOpt = productoVarianteRepository
                .findByProductoAndTallaAndColor(producto, tallaNorm, colorNorm);

        ProductoVariante variante;
        boolean esNueva = false;

        if (varianteOpt.isPresent()) {
            // --- EDITANDO EXISTENTE ---
            variante = varianteOpt.get();
            variante.setActivo(true);
        } else {
            // --- CREANDO NUEVA ---
            esNueva = true;
            variante = new ProductoVariante();
            variante.setProducto(producto);
            variante.setTalla(tallaNorm);
            variante.setColor(colorNorm);
            variante.setStock(0); // Inicia en 0 hasta que hagas una entrada de inventario
            variante.setActivo(true);

            // Si no suben foto nueva, hereda la del padre al nacer
            if (imagenUrl == null || imagenUrl.isEmpty()) {
                variante.setImagenUrl(producto.getImagenUrl());
            }
        }

        // 4. ACTUALIZACIÓN DE IMAGEN (Lógica corregida)
        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            // A. Asignar a la variante actual
            variante.setImagenUrl(imagenUrl);

            // B. Actualizar en CASCADA a otras variantes del mismo color (para coherencia visual)
            if (producto.getVariantes() != null) {
                for (ProductoVariante v : producto.getVariantes()) {
                    // Actualizamos todas las que tengan el mismo color
                    if (v.getColor().equalsIgnoreCase(colorNorm)) {
                        v.setImagenUrl(imagenUrl);
                    }
                }
            }
        }

        // --- CORRECCIÓN CLAVE ---
        // Si es nueva, DEBEMOS agregarla explícitamente a la lista del padre
        // antes de guardar, para que CascadeType.ALL funcione correctamente.
        if (esNueva) {
            producto.getVariantes().add(variante);
        }

        // 5. Guardar el Producto Padre (esto guardará/actualizará la variante por cascada)
        productoRepository.save(producto);
    }

    @Transactional
    public void toggleVariantesPorColor(Long productoId, String color) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        List<ProductoVariante> variantesDelColor = producto.getVariantes().stream()
                .filter(v -> v.getColor().equalsIgnoreCase(color))
                .toList();

        if (variantesDelColor.isEmpty()) return;

        boolean hayActivas = variantesDelColor.stream().anyMatch(ProductoVariante::getActivo);
        boolean nuevoEstado = !hayActivas;

        for (ProductoVariante v : variantesDelColor) {
            v.setActivo(nuevoEstado);
        }

        productoRepository.save(producto);
    }

    @Transactional
    public void eliminarVariante(Long varianteId) {
        ProductoVariante variante = productoVarianteRepository.findById(varianteId)
                .orElseThrow(() -> new RuntimeException("Variante no encontrada"));

        Producto producto = variante.getProducto();

        // Eliminar variante de la BD
        productoVarianteRepository.delete(variante);

        // Remover de la lista en memoria del padre para mantener consistencia
        producto.getVariantes().remove(variante);
        productoRepository.save(producto);
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return texto;
        }
        String limpio = texto.trim().toLowerCase();
        return limpio.substring(0, 1).toUpperCase() + limpio.substring(1);
    }
}