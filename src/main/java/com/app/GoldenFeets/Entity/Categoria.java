package com.app.GoldenFeets.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una categoría de productos (ej. Deportivos, Formales).
 * Cada instancia de esta clase corresponde a una fila en la tabla "categorias".
 */
@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    /**
     * Identificador único para cada categoría. Clave primaria.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de la categoría. Debe ser único para evitar duplicados.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    /**
     * Descripción opcional de la categoría.
     */
    @Column(length = 255)
    private String descripcion;

    /**
     * Relación One-to-Many: Una categoría puede tener muchos productos.
     */
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Producto> productos = new ArrayList<>();

}