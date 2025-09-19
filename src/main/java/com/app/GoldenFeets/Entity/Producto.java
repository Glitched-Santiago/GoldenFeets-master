package com.app.GoldenFeets.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un producto (zapato) en la base de datos.
 * Cada instancia de esta clase corresponde a una fila en la tabla "productos".
 */
@Entity
@Table(name = "productos")
@Data // Anotación de Lombok: genera getters, setters, toString, equals y hashCode.
@NoArgsConstructor // Anotación de Lombok: genera un constructor sin argumentos (requerido por JPA).
@AllArgsConstructor // Anotación de Lombok: genera un constructor con todos los argumentos.
public class Producto {

    /**
     * Identificador único para cada producto.
     * Es la clave primaria de la tabla.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY es una buena estrategia para que la BD (MySQL, H2, etc.) auto-incremente el valor.
    private Long id;

    /**
     * Nombre del producto. No puede ser nulo y tiene una longitud máxima de 150 caracteres.
     */
    @Column(nullable = false, length = 150)
    private String nombre;

    /**
     * Descripción detallada del producto. Se mapea a un tipo de dato TEXT para textos largos.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Precio de venta del producto. No puede ser nulo.
     */
    @Column(nullable = false)
    private Double precio;

    /**
     * Cantidad de unidades disponibles en inventario. No puede ser nulo.
     */
    @Column(nullable = false)
    private Integer stock;

    /**
     * Talla del zapato. Se guarda como String para dar flexibilidad (ej: "42 EU", "9.5 US").
     */
    @Column(length = 20)
    private String talla;

    /**
     * Color principal del producto.
     */
    @Column(length = 50)
    private String color;

    /**
     * URL o ruta donde se encuentra la imagen del producto.
     */
    @Column(name = "imagen_url", length = 255) // Es buena práctica nombrar las columnas en snake_case.
    private String imagenUrl;




    @ManyToOne(fetch = FetchType.LAZY) // Un producto pertenece a una categoría.
    @JoinColumn(name = "categoria_id") // Nombre de la columna de la clave foránea.
    private Categoria categoria;


}