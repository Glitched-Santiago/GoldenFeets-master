package com.app.GoldenFeets.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    @ToString.Exclude
    private Categoria categoria;

    // --- RELACIÓN NUEVA ---
    // Un producto tiene muchas variantes (tallas/colores)
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoVariante> variantes = new ArrayList<>();

    // --- CAMPOS ELIMINADOS ---
    // Se borraron: stock, talla, color (ahora viven en 'variantes')

    // --- MÉTODO ÚTIL PARA LA VISTA ---
    // Calcula el stock total sumando todas las variantes
    public Integer getStockTotal() {
        if (variantes == null || variantes.isEmpty()) {
            return 0;
        }
        return variantes.stream().mapToInt(ProductoVariante::getStock).sum();
    }
}