package com.app.GoldenFeets.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
/**
 * Representa los posibles estados de un Pedido.
 */

public enum EstadoPedido {
    PENDIENTE,
    PAGADO,
    COMPLETADO,
    CANCELADO
}