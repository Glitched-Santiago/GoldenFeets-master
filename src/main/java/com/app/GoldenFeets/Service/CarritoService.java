package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Producto;
import com.app.GoldenFeets.Model.CarritoItem;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CarritoService {

    // Se restaura la declaración del mapa de items.
    private final Map<Long, CarritoItem> items = new HashMap<>();

    public void agregarProducto(Producto producto, int cantidad) {
        Long productoId = producto.getId();
        CarritoItem itemExistente = items.get(productoId);

        if (itemExistente != null) {
            itemExistente.setCantidad(itemExistente.getCantidad() + cantidad);
        } else {
            items.put(productoId, new CarritoItem(producto, cantidad));
        }
    }

    public void removerProducto(Long productoId) {
        items.remove(productoId);
    }

    // MÉTODO RESTAURADO: Para aumentar o disminuir la cantidad de un producto.
    public void actualizarCantidad(Long productoId, int cantidad) {
        CarritoItem item = items.get(productoId);
        if (item != null) {
            // Se permite que la cantidad sea 0, pero no negativa.
            if (cantidad >= 0) {
                item.setCantidad(cantidad);
            }
        }
    }

    public Map<Long, CarritoItem> getItems() {
        return items;
    }

    public Double getTotal() {
        // Se calcula el total solo de los productos con cantidad mayor a 0.
        return items.values().stream()
                .filter(item -> item.getCantidad() > 0)
                .mapToDouble(CarritoItem::getSubtotal)
                .sum();
    }

    public void limpiar() {
        items.clear();
    }
}