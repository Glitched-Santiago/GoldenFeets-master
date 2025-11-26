package com.app.GoldenFeets.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "producto_variantes")
@Data // <--- Esto genera automáticamente el getImagenUrl()
@NoArgsConstructor
@AllArgsConstructor
public class ProductoVariante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String color;

    @Column(length = 20)
    private String talla;

    @Column(nullable = false)
    private Integer stock;

    private Boolean activo = true;

    // --- AGREGA ESTO ---
    @Column(name = "imagen_url")
    private String imagenUrl;
    // -------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    @ToString.Exclude
    private Producto producto;
}