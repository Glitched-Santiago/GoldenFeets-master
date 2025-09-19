package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.app.GoldenFeets.Model.CarritoItem;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Encuentra todos los pedidos realizados por un cliente específico,
     * ordenados por la fecha de creación de forma descendente (los más nuevos primero).
     *
     * @param cliente El cliente cuyos pedidos se quieren buscar.
     * @return Una lista de pedidos del cliente.
     */
    List<Pedido> findByClienteOrderByFechaCreacionDesc(Cliente cliente);

}