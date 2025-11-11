package com.app.GoldenFeets.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_entradas")
@Data
@NoArgsConstructor
public class InventarioEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 200)
    private String distribuidor;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, length = 50)
    private String color;

    // --- NUEVOS CAMPOS ---
    @Column(name = "precio_costo_unitario", nullable = false, columnDefinition = "double default 0.0")
    private Double precioCostoUnitario = 0.0;

    @Column(name = "costo_total_entrada", nullable = false, columnDefinition = "double default 0.0")
    private Double costoTotalEntrada = 0.0;
    // --- FIN DE NUEVOS CAMPOS ---

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        // Calculamos el costo total al guardar
        if (this.cantidad != null && this.precioCostoUnitario != null) {
            this.costoTotalEntrada = this.cantidad * this.precioCostoUnitario;
        }
    }
}