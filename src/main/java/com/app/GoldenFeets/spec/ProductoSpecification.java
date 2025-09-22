package com.app.GoldenFeets.spec;

import com.app.GoldenFeets.Entity.Categoria;
import com.app.GoldenFeets.Entity.Producto;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ProductoSpecification {

    public static Specification<Producto> findByCriteria(
            String keyword, Long categoriaId, Double precioMin, Double precioMax) {

        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

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

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}