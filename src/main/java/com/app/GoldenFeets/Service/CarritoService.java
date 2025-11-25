package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Entity.ProductoVariante;
import com.app.GoldenFeets.Model.CarritoItem;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CarritoService {

    // --- CAMBIO IMPORTANTE ---
    // La clave del Mapa ahora es el ID de la VARIANTE (Long productoVarianteId), no el ID del producto.
    // Esto permite tener el mismo zapato en dos tallas diferentes como ítems separados.
    private final Map<Long, CarritoItem> items = new HashMap<>();

    /**
     * Agrega un producto al carrito.
     * Ahora requiere la Variante específica (Talla/Color).
     */
    public void agregarProducto(Producto producto, ProductoVariante variante, int cantidad) {
        // Validamos que la variante exista
        if (variante == null) {
            throw new IllegalArgumentException("No se puede agregar al carrito sin especificar una variante (Talla/Color).");
        }

        Long key = variante.getId(); // Usamos el ID de la variante como clave única
        CarritoItem itemExistente = items.get(key);

        if (itemExistente != null) {
            // Si ya existe esa talla exacta, sumamos la cantidad
            itemExistente.setCantidad(itemExistente.getCantidad() + cantidad);
        } else {
            // Si es nueva, usamos el nuevo constructor que creaste
            items.put(key, new CarritoItem(producto, variante, cantidad));
        }
    }

    /**
     * Remueve un ítem del carrito.
     * Recibe el ID de la Variante, ya que es la clave del mapa.
     */
    public void removerProducto(Long productoVarianteId) {
        items.remove(productoVarianteId);
    }

    /**
     * Actualiza la cantidad de un ítem específico.
     * Recibe el ID de la Variante.
     */
    public void actualizarCantidad(Long productoVarianteId, int cantidad) {
        CarritoItem item = items.get(productoVarianteId);
        if (item != null) {
            if (cantidad >= 0) {
                item.setCantidad(cantidad);
            }
        }
    }

    public Map<Long, CarritoItem> getItems() {
        return items;
    }

    public Double getTotal() {
        return items.values().stream()
                .filter(item -> item.getCantidad() > 0)
                .mapToDouble(CarritoItem::getSubtotal)
                .sum();
    }

    public void limpiar() {
        items.clear();
    }
}