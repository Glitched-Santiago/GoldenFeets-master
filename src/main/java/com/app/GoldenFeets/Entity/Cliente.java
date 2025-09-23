package com.app.GoldenFeets.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@DiscriminatorValue("CLIENTE") // Esto le dice a JPA que cuando el rol sea "CLIENTE", debe crear esta clase
@Data
@EqualsAndHashCode(callSuper = true) // Importante para la herencia con Lombok
public class Cliente extends Usuario {

    // --- LA RELACIÓN CLAVE ---
    // Un cliente puede tener muchos pedidos.
    // 'mappedBy = "cliente"' indica que la entidad Pedido gestiona la relación.
    @OneToMany(mappedBy = "cliente")
    private List<Pedido> pedidos;

    // No necesita atributos adicionales, ya que los hereda todos de Usuario.
}