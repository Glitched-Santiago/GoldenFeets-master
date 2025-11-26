package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Entity.ProductoVariante;
import com.app.GoldenFeets.Repository.ProductoRepository;
import com.app.GoldenFeets.spec.ProductoSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    // --- CORRECCIÓN DEL ERROR AQUÍ ---
    public List<Producto> obtenerProductosDisponibles() {
        // CORRECCIÓN: Usamos la consulta personalizada que verifica el stock en las variantes
        return productoRepository.findAllConStock();
    }

    // Asegúrate también que tu método 'search' esté usando el searchByFilters del paso anterior:
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
    public void crearVarianteManual(Long productoId, String talla, String color) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Validar si ya existe para no duplicar
        boolean existe = producto.getVariantes().stream()
                .anyMatch(v -> v.getTalla().equalsIgnoreCase(talla) && v.getColor().equalsIgnoreCase(color));

        if (!existe) {
            ProductoVariante nueva = new ProductoVariante();
            nueva.setProducto(producto);
            nueva.setTalla(talla.trim());
            nueva.setColor(color.trim());
            nueva.setStock(0); // Stock inicial 0
            nueva.setActivo(true); // Visible en catálogo (aunque sin stock saldrá agotado)

            producto.getVariantes().add(nueva);
            productoRepository.save(producto);
        } else {
            // Opcional: Si ya existe pero estaba oculta, podrías reactivarla aquí
            throw new RuntimeException("Esa combinación de Talla y Color ya existe.");
        }
    }
    @Transactional
    public void toggleVariantesPorColor(Long productoId, String color) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 1. Filtrar todas las variantes de ese color específico
        List<ProductoVariante> variantesDelColor = producto.getVariantes().stream()
                .filter(v -> v.getColor().equalsIgnoreCase(color))
                .toList();

        if (variantesDelColor.isEmpty()) return;

        // 2. Determinar qué hacer:
        // Si al menos UNA está activa, la intención es APAGAR TODO.
        // Si TODAS están apagadas, la intención es PRENDER TODO.
        boolean hayActivas = variantesDelColor.stream().anyMatch(ProductoVariante::getActivo);
        boolean nuevoEstado = !hayActivas; // Invertir lógica

        // 3. Aplicar el cambio a todas
        for (ProductoVariante v : variantesDelColor) {
            v.setActivo(nuevoEstado);
        }

        productoRepository.save(producto);
    }
}