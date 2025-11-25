// src/main/java/com/app/GoldenFeets/DTO/InventarioSalidaDTO.java
package com.app.GoldenFeets.DTO;

import lombok.Data;

@Data
public class InventarioSalidaDTO {
    private Long productoId;
    private Integer cantidad;
    private String motivo; // Ej: "Ajuste", "Pérdida", "Devolución a proveedor"

    // --- NUEVOS CAMPOS ---
    // Necesarios para saber a qué variante específica restar el stock
    private String talla;
    private String color;
}