// Revisa: com/app/GoldenFeets/Repository/InventarioEntradaRepository.java

package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.InventarioEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- ¡QUE NO FALTE ESTE!
import java.util.List; // <-- ¡QUE NO FALTE ESTE!

public interface InventarioEntradaRepository extends JpaRepository<InventarioEntrada, Long> {

    @Query("SELECT e FROM InventarioEntrada e LEFT JOIN FETCH e.producto")
    List<InventarioEntrada> findAllWithProducto();

}