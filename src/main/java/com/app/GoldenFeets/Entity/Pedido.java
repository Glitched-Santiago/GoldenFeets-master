package com.app.GoldenFeets.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;

    @ManyToOne // Un pedido pertenece a un cliente
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Un pedido tiene muchos detalles (productos)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalle> detalles = new ArrayList<>();

    private Double total;

    @PrePersist
    public void onPrePersist() {
        fechaCreacion = LocalDateTime.now();
        estado = EstadoPedido.PENDIENTE; // Estado inicial
    }
}
