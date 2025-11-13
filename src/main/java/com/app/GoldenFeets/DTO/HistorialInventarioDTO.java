// src/main/java/com/app/GoldenFeets/DTO/HistorialInventarioDTO.java
package com.app.GoldenFeets.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HistorialInventarioDTO {
    private LocalDateTime fecha;
    private String productoNombre;
    private String tipo; // "Entrada" o "Salida"
    private Integer cantidad;
    private String descripcion; // Distribuidor, Motivo o Venta
    private Long pedidoId; // Opcional
    private Double precioUnitario;
    private Double valorMonetario;
}