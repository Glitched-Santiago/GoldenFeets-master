package com.app.GoldenFeets.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pedido_detalles")
@Data
public class PedidoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Muchos detalles pertenecen a un pedido
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne // La línea de detalle referencia a un producto
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    private Integer cantidad;
    private Double precioUnitario; // Guardamos el precio al momento de la compra
}