package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, Long> {

    // Top 5 productos más vendidos (Agrupa por nombre y suma cantidad)
    @Query("SELECT p.nombre, SUM(d.cantidad) as total " +
            "FROM PedidoDetalle d JOIN d.producto p " +
            "GROUP BY p.nombre " +
            "ORDER BY total DESC " +
            "LIMIT 5")
    List<Object[]> encontrarTopProductosVendidos();
}