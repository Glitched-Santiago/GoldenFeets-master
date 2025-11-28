package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Entity.ProductoVariante;
import com.app.GoldenFeets.Repository.ProductoRepository;
import com.app.GoldenFeets.Repository.ProductoVarianteRepository;
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
    private final ProductoVarianteRepository productoVarianteRepository;

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
    public void crearVarianteManual(Long productoId, String talla, String color, String imagenUrl) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 1. Limpieza de datos
        String tallaClean = talla.trim();
        String colorClean = color.trim();
        String imgClean = (imagenUrl != null && !imagenUrl.isBlank()) ? imagenUrl.trim() : null;

        // 2. Buscar si ya existe esa combinación
        Optional<ProductoVariante> varianteExistente = producto.getVariantes().stream()
                .filter(v -> v.getTalla().equalsIgnoreCase(tallaClean) && v.getColor().equalsIgnoreCase(colorClean))
                .findFirst();

        if (varianteExistente.isPresent()) {
            // --- CASO A: YA EXISTE -> ACTUALIZAMOS ---
            ProductoVariante v = varianteExistente.get();
            System.out.println("DEBUG: Actualizando variante existente ID: " + v.getId());

            // Actualizamos la imagen si nos enviaron una nueva
            if (imgClean != null) {
                v.setImagenUrl(imgClean);
            }

            // Nos aseguramos que esté visible
            v.setActivo(true);

            // Guardamos (al estar dentro de una transacción, esto actualiza la BD)
            productoRepository.save(producto); // Guarda el padre y sus hijos

        } else {
            // --- CASO B: NO EXISTE -> CREAMOS ---
            System.out.println("DEBUG: Creando nueva variante");

            ProductoVariante nueva = new ProductoVariante();
            nueva.setProducto(producto);
            nueva.setTalla(tallaClean);
            nueva.setColor(colorClean);
            nueva.setImagenUrl(imgClean); // Guardamos la imagen
            nueva.setStock(0);
            nueva.setActivo(true);

            producto.getVariantes().add(nueva);
            productoRepository.save(producto);
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

    @Transactional
    public void eliminarVariante(Long varianteId) {
        // Buscamos la variante
        ProductoVariante variante = productoVarianteRepository.findById(varianteId)
                .orElseThrow(() -> new RuntimeException("Variante no encontrada"));

        // Obtenemos el producto padre para actualizarlo luego si es necesario
        Producto producto = variante.getProducto();

        // Eliminamos la variante
        // Nota: Si hay pedidos o movimientos de inventario ligados a esta variante,
        // la base de datos podría lanzar un error de integridad referencial (Foreign Key).
        // En ese caso, lo ideal sería 'desactivarla' (activo = false) en lugar de borrarla.
        productoVarianteRepository.delete(variante);

        // Quitamos la variante de la lista del padre para mantener la coherencia en memoria
        producto.getVariantes().remove(variante);
        productoRepository.save(producto);
    }
}