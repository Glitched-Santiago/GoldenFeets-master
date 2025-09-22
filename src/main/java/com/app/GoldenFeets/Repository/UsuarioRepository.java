package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 1. IMPORT NECESARIO
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// 2. AÑADIMOS JpaSpecificationExecutor<Usuario>
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmail(String email);

    // 3. El método de búsqueda anterior ya no es necesario,
    // ya que toda la lógica de búsqueda ahora está en UsuarioSpecification.
    // List<Usuario> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(String nombre, String email);
}