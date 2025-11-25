package com.app.GoldenFeets.DTO;

import lombok.Data;
import java.util.List;

@Data
public class ProductoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private String imagenUrl;

    // El stock ahora es la suma total de todas las variantes
    private Integer stockTotal;

    // Lista de variantes disponibles (ej: 40-Rojo, 41-Azul)
    private List<VarianteDTO> variantes;

    // Clase interna para manejar las variantes dentro del DTO
    @Data
    public static class VarianteDTO {
        private Long id;
        private String talla;
        private String color;
        private Integer stock;
    }
}