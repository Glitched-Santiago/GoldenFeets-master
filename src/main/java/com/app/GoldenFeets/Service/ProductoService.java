package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Repository.ProductoRepository;
import com.app.GoldenFeets.spec.ProductoSpecification; // Importante mantener el import
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
    // --- Se elimina la dependencia de ProductoSpecification ---

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerProductosDisponibles() {
        return productoRepository.findAll()
                .stream()
                .filter(p -> p.getStock() > 0)
                .collect(Collectors.toList());
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

    // --- MÉTODO DE BÚSQUEDA CORREGIDO ---
    public List<Producto> search(String keyword, Long categoriaId, Double precioMin, Double precioMax, String talla, String color) {
        // --- Llamada estática directa a la clase Specification ---
        Specification<Producto> spec = ProductoSpecification.findByCriteria(keyword, categoriaId, precioMin, precioMax, talla, color);
        return productoRepository.findAll(spec);
    }
}

