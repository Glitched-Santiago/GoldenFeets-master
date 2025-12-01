package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Entity.ProductoVariante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Long> {

    // Este método busca la variante exacta combinando Producto + Talla + Color
    Optional<ProductoVariante> findByProductoAndTallaAndColor(Producto producto, String talla, String color);
    List<ProductoVariante> findByStockLessThanAndActivoTrue(Integer stockMinimo);
}