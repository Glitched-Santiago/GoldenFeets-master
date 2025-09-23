package com.app.GoldenFeets.Repository.spec;

import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.Pedido;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class PedidoSpecification {

    public Specification<Pedido> findByCriteria(String clienteNombre, LocalDate fechaDesde, LocalDate fechaHasta) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // --- Filtro por Nombre de Cliente ---
            if (clienteNombre != null && !clienteNombre.trim().isEmpty()) {
                Join<Pedido, Cliente> clienteJoin = root.join("cliente");
                Predicate predicateNombres = criteriaBuilder.like(criteriaBuilder.lower(clienteJoin.get("nombres")), "%" + clienteNombre.toLowerCase() + "%");
                Predicate predicateApellidos = criteriaBuilder.like(criteriaBuilder.lower(clienteJoin.get("apellidos")), "%" + clienteNombre.toLowerCase() + "%");
                predicates.add(criteriaBuilder.or(predicateNombres, predicateApellidos));
            }

            // --- Filtro por Rango de Fechas ---
            if (fechaDesde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaDesde.atStartOfDay()));
            }
            if (fechaHasta != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fechaCreacion"), fechaHasta.atTime(LocalTime.MAX)));
            }

            // Ordena por fecha descendente por defecto
            query.orderBy(criteriaBuilder.desc(root.get("fechaCreacion")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}