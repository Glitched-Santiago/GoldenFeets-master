package com.app.GoldenFeets.DTO;

import lombok.Data;

@Data
public class EstadisticasInventarioDTO {

    private Double totalGastado;
    private Double totalIngresado;

    // --- ESTE CAMPO FALTABA ---
    private Double beneficioNeto;
}