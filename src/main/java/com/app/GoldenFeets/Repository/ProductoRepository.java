package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Importar
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// Añadir JpaSpecificationExecutor<Producto>
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    // El método antiguo ya no es necesario para la búsqueda principal
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    @Query(value = "SELECT * FROM productos ORDER BY RAND() LIMIT :limite", nativeQuery = true)
    List<Producto> findRandomProductos(@Param("limite") int limite);
}