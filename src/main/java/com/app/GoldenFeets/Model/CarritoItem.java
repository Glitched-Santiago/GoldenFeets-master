package com.app.GoldenFeets.Model;

import com.app.GoldenFeets.Entity.Producto;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable; // <-- IMPORTANTE

@Data
@NoArgsConstructor
// AÑADIMOS "implements Serializable"
public class CarritoItem implements Serializable {

    // AÑADIMOS UN serialVersionUID, es una buena práctica para clases serializables.
    private static final long serialVersionUID = 1L;

    private Long productoId;
    private String nombre;
    private String imagenUrl;
    private Double precio;
    private int cantidad;
    private Double subtotal;

    public CarritoItem(Producto producto, int cantidad) {
        this.productoId = producto.getId();
        this.nombre = producto.getNombre();
        this.imagenUrl = producto.getImagenUrl();
        this.precio = producto.getPrecio();
        this.cantidad = cantidad;
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