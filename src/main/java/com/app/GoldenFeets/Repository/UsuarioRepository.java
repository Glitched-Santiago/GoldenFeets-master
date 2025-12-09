package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 1. IMPORT NECESARIO
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
// 2. AÑADIMOS JpaSpecificationExecutor<Usuario>
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByRol(String rol);

}