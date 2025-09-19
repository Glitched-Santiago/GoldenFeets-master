package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, Long> {
    // De momento no se necesitan métodos personalizados.
    // Los detalles se gestionarán a través de la entidad Pedido.
}