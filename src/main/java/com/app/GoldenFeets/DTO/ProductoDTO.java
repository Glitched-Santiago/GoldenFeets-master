package com.app.GoldenFeets.DTO;

import lombok.Data;

@Data
public class ProductoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private String talla;
    private String color;
    private String imagenUrl;

    // Podrías añadir el nombre de la categoría si tuvieras esa relación
    // private String nombreCategoria;

}