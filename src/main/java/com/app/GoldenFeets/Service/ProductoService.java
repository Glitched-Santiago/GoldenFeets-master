package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Repository.ProductoRepository;
import com.app.GoldenFeets.spec.ProductoSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Necesitarás este import

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;
    private final ProductoSpecification productoSpecification;

    public ProductoService(ProductoSpecification productoSpecification) {
        this.productoSpecification = productoSpecification;
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // NUEVO MÉTODO
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


    // Dentro de la clase ProductoService
    public List<Producto> search(String keyword, Long categoriaId, Double precioMin, Double precioMax) {
        Specification<Producto> spec = productoSpecification.findByCriteria(keyword, categoriaId, precioMin, precioMax);
        return productoRepository.findAll(spec);
    }

}