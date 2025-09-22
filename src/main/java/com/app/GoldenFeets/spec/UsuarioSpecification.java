package com.app.GoldenFeets.spec;

import com.app.GoldenFeets.Entity.Administrador;
import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.Usuario;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UsuarioSpecification {

    // MÉTODO ACTUALIZADO PARA INCLUIR EL CAMPO EMAIL
    public static Specification<Usuario> findByCriteria(
            String id, String nombres, String apellidos, String numeroDocumento, String email, String rol) {

        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (id != null && !id.trim().isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("id"), Long.parseLong(id)));
                } catch (NumberFormatException e) { /* Ignorar si no es un número */ }
            }

            if (nombres != null && !nombres.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nombres")), "%" + nombres.toLowerCase() + "%"));
            }

            if (apellidos != null && !apellidos.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("apellidos")), "%" + apellidos.toLowerCase() + "%"));
            }

            if (numeroDocumento != null && !numeroDocumento.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("numeroDocumento")), "%" + numeroDocumento.toLowerCase() + "%"));
            }

            // FILTRO POR EMAIL AÑADIDO
            if (email != null && !email.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }

            if (rol != null && !rol.trim().isEmpty()) {
                if ("CLIENTE".equalsIgnoreCase(rol)) {
                    predicates.add(cb.equal(root.type(), Cliente.class));
                } else if ("ADMIN".equalsIgnoreCase(rol)) {
                    predicates.add(cb.equal(root.type(), Administrador.class));
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}