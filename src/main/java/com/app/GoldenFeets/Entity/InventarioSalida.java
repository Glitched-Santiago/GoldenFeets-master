// src/main/java/com/app/GoldenFeets/Entity/InventarioSalida.java
package com.app.GoldenFeets.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_salidas")
@Data
@NoArgsConstructor
public class InventarioSalida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, length = 100)
    private String motivo; // Ej: "Venta", "Ajuste manual", "Pérdida"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = true) // Clave para vincular a ventas
    private Pedido pedido;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "stock_anterior")
    private Integer stockAnterior;

    @Column(name = "stock_nuevo")
    private Integer stockNuevo;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}