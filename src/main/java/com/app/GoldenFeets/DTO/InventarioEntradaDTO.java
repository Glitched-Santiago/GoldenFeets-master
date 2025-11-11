package com.app.GoldenFeets.DTO;

import lombok.Data;

@Data
public class InventarioEntradaDTO {
    private Long productoId;
    private String distribuidor;
    private Integer cantidad;
    private String color;
    private Double precioCostoUnitario; // <-- NUEVO CAMPO
}