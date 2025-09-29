package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    @Query("SELECT p FROM Producto p WHERE " +
            "(:keyword IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
            "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
            "(:precioMax IS NULL OR p.precio <= :precioMax) AND " +
            "(:talla IS NULL OR LOWER(p.talla) LIKE LOWER(CONCAT('%', :talla, '%'))) AND " +
            "(:color IS NULL OR LOWER(p.color) LIKE LOWER(CONCAT('%', :color, '%')))")
    List<Producto> searchByFilters(
            @Param("keyword") String keyword,
            @Param("categoriaId") Long categoriaId,
            @Param("precioMin") Double precioMin,
            @Param("precioMax") Double precioMax,
            @Param("talla") String talla,
            @Param("color") String color
    );
    @Query(value = "SELECT * FROM productos ORDER BY RAND() LIMIT :limite", nativeQuery = true)
    List<Producto> findRandomProductos(@Param("limite") int limite);

}

