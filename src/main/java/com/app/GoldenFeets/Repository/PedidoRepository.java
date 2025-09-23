package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.Pedido;
import com.app.GoldenFeets.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Asegúrate de tener este import
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
//                                  AÑADE ESTA PARTE
//                                        V
public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

    List<Pedido> findByClienteOrderByFechaCreacionDesc(Cliente cliente);

    List<Pedido> findAllByOrderByFechaCreacionDesc();
}
