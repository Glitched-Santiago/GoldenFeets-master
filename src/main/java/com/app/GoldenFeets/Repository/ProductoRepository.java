package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // ... otros métodos (searchByFilters, findRandomProductos) ...

    // NUEVO: Busca productos que tengan al menos una variante con stock > 0
    @Query("SELECT DISTINCT p FROM Producto p JOIN p.variantes v WHERE v.stock > 0 AND v.activo = true")
    List<Producto> findAllConStock();

    // --- 1. MÉTODO DE BÚSQUEDA AVANZADA (Arreglado para Variantes) ---
    @Query("SELECT DISTINCT p FROM Producto p " +
            "LEFT JOIN p.variantes v " +
            "WHERE (:keyword IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId) " +
            "AND (:precioMin IS NULL OR p.precio >= :precioMin) " +
            "AND (:precioMax IS NULL OR p.precio <= :precioMax) " +
            "AND (:talla IS NULL OR LOWER(v.talla) LIKE LOWER(CONCAT('%', :talla, '%'))) " +
            "AND (:color IS NULL OR LOWER(v.color) LIKE LOWER(CONCAT('%', :color, '%'))) " +
            "AND (v.activo = true)")

    List<Producto> searchByFilters(
            @Param("keyword") String keyword,
            @Param("categoriaId") Long categoriaId,
            @Param("precioMin") Double precioMin,
            @Param("precioMax") Double precioMax,
            @Param("talla") String talla,
            @Param("color") String color
    );

    // --- 2. MÉTODO PARA PRODUCTOS ALEATORIOS (Nuevo) ---
    // Usamos nativeQuery = true para aprovechar la función RAND() de MySQL
    @Query(value = "SELECT * FROM productos ORDER BY RAND() LIMIT :limite", nativeQuery = true)
    List<Producto> findRandomProductos(@Param("limite") int limite);

    // --- 3. MÉTODO PARA ADMIN (Ordenado por ID) ---
    List<Producto> findAllByOrderByIdDesc();
}