package com.app.GoldenFeets.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 50)
    private EstadoPedido estado;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalle> detalles = new ArrayList<>();

    private Double total;

    // --- NUEVOS CAMPOS PARA DATOS DE ENVÍO ---
    private String envioNombres;    // Nombres de quien recibe
    private String envioApellidos;  // Apellidos de quien recibe
    private String envioTelefono;   // Celular de contacto
    private String envioCiudad;     // Ej: Bogotá, D.C.
    private String envioLocalidad;  // Ej: Suba
    private String envioDireccion;  // Ej: Calle 123...

    @PrePersist
    public void onPrePersist() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) estado = EstadoPedido.PAGADO;
    }
}