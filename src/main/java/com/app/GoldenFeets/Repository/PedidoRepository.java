// Revisa: com/app/GoldenFeets/Repository/PedidoRepository.java

package com.app.GoldenFeets.Repository;

import com.app.GoldenFeets.Entity.EstadoPedido; // <-- ¡CLAVE!
import com.app.GoldenFeets.Entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- ¡CLAVE!
import org.springframework.data.repository.query.Param; // <-- ¡CLAVE!

import java.time.LocalDateTime;
import java.util.List; // <-- ¡CLAVE!
import com.app.GoldenFeets.Entity.Cliente;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// Asegúrate que tu interfaz ya no tenga el error de "EstadoPedido"
public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido>{

    // (Aquí deben estar tus otros métodos, como findByClienteOrderByFechaCreacionDesc)
    List<Pedido> findByClienteOrderByFechaCreacionDesc(Cliente cliente);
    List<Pedido> findAllByOrderByFechaCreacionDesc();

    // ¡Este es el método nuevo que pusimos!
    @Query("SELECT p FROM Pedido p WHERE p.estado = :estado")
    List<Pedido> findAllByEstado(@Param("estado") EstadoPedido estado);

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.fechaCreacion BETWEEN :inicio AND :fin AND p.estado = 'PAGADO'")
    Double sumarVentasPorRango(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Contar pedidos de hoy
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.fechaCreacion BETWEEN :inicio AND :fin")
    Integer contarPedidosPorRango(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Obtener los últimos 5 pedidos
    List<Pedido> findTop5ByOrderByFechaCreacionDesc();

}