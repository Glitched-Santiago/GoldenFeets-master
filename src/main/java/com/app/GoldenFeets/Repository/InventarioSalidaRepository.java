// src/main/java/com/app/GoldenFeets/Repository/InventarioSalidaRepository.java
package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.InventarioSalida;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface InventarioSalidaRepository extends JpaRepository<InventarioSalida, Long> {
    @Query("SELECT s FROM InventarioSalida s " +
            "LEFT JOIN FETCH s.pedido p " +
            "LEFT JOIN FETCH p.detalles d")
    List<InventarioSalida> findAllWithPedidoAndDetalles();
}