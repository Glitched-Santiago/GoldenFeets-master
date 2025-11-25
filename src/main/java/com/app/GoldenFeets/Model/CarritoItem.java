package com.app.GoldenFeets.Model;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Entity.ProductoVariante; // <-- IMPORTANTE: Importar la nueva entidad
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class CarritoItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productoId;
    private String nombre;
    private String imagenUrl;
    private Double precio;
    private int cantidad;
    private Double subtotal;

    // --- NUEVOS CAMPOS PARA SOPORTAR VARIANTES ---
    private Long productoVarianteId; // ID único de la combinación (ej: ID 55 = Nike Air - Talla 40 - Rojo)
    private String talla;
    private String color;

    // --- CONSTRUCTOR ACTUALIZADO ---
    // Ahora recibe también el objeto ProductoVariante
    public CarritoItem(Producto producto, ProductoVariante variante, int cantidad) {
        this.productoId = producto.getId();
        this.nombre = producto.getNombre();
        this.imagenUrl = producto.getImagenUrl();
        this.precio = producto.getPrecio();
        this.cantidad = cantidad;

        // Guardamos los datos de la variante seleccionada
        if (variante != null) {
            this.productoVarianteId = variante.getId();
            this.talla = variante.getTalla();
            this.color = variante.getColor();
        }

        this.actualizarSubtotal();
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        this.actualizarSubtotal();
    }

    private void actualizarSubtotal() {
        this.subtotal = this.precio * this.cantidad;
    }
}