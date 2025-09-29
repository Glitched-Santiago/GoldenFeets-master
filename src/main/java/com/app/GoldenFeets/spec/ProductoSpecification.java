package com.app.GoldenFeets.spec;

import com.app.GoldenFeets.Entity.Categoria;
import com.app.GoldenFeets.Entity.Producto;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

// No necesita la anotación @Component
public class ProductoSpecification {

    /**
     * Crea una Specification para buscar productos con múltiples criterios.
     * Este método es estático para ser llamado directamente desde el servicio.
     */
    public static Specification<Producto> findByCriteria(
            String keyword, Long categoriaId, Double precioMin, Double precioMax, String talla, String color) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por palabra clave (busca en nombre y descripción)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String lowerCaseKeyword = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")), lowerCaseKeyword),
                        cb.like(cb.lower(root.get("descripcion")), lowerCaseKeyword)
                ));
            }

            // Filtro por categoría
            if (categoriaId != null) {
                Join<Producto, Categoria> categoriaJoin = root.join("categoria");
                predicates.add(cb.equal(categoriaJoin.get("id"), categoriaId));
            }

            // Filtro por precio mínimo
            if (precioMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("precio"), precioMin));
            }

            // Filtro por precio máximo
            if (precioMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
            }

            // Filtro por talla
            if (talla != null && !talla.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("talla")), talla.toLowerCase()));
            }

            // Filtro por color
            if (color != null && !color.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("color")), color.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

