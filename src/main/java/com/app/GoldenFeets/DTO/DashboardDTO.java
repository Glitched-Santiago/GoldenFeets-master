package com.app.GoldenFeets.DTO;

import com.app.GoldenFeets.Entity.Pedido;
import com.app.GoldenFeets.Entity.ProductoVariante;
import lombok.Data;
import java.util.List;
import java.util.ArrayList; // Importante

@Data
public class DashboardDTO {
    // Tarjetas Superiores
    private Double ventasHoy;
    private Integer pedidosHoy;
    private Double ingresosMes;
    private Integer usuariosRegistrados;

    // Listas para Tablas
    private List<Pedido> ultimosPedidos;
    private List<ProductoVariante> productosBajoStock;

    // --- CAMBIO AQUÍ: Usamos Listas simples para el gráfico ---
    private List<String> topLabels = new ArrayList<>();  // Nombres de productos
    private List<Long> topValues = new ArrayList<>();    // Cantidades
}