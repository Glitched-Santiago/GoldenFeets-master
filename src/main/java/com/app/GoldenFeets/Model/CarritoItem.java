package com.app.GoldenFeets.Model;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Entity.ProductoVariante;
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

    // Campos de la Variante
    private Long productoVarianteId;
    private String talla;
    private String color;

    public CarritoItem(Producto producto, ProductoVariante variante, int cantidad) {
        this.productoId = producto.getId();
        this.nombre = producto.getNombre();

        // Lógica inteligente de imagen:
        // Si la variante tiene foto específica, usamos esa. Si no, la del producto.
        if (variante != null && variante.getImagenUrl() != null && !variante.getImagenUrl().isEmpty()) {
            this.imagenUrl = variante.getImagenUrl();
        } else {
            this.imagenUrl = producto.getImagenUrl();
        }

        this.precio = producto.getPrecio();
        this.cantidad = cantidad;

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