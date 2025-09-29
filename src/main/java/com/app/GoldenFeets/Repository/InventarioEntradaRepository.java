package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.InventarioEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventarioEntradaRepository extends JpaRepository<InventarioEntrada, Long> {
}

