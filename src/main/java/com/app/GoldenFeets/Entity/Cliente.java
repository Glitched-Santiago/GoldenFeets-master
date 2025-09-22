package com.app.GoldenFeets.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("CLIENTE")
@Data
@EqualsAndHashCode(callSuper = true)
public class Cliente extends Usuario {

    // El cuerpo de esta clase ahora está vacío, ya que todos los campos
    // necesarios se heredan de la clase padre Usuario.
    // Si en el futuro un Cliente necesita un campo que un Admin no tiene, se añadiría aquí.

}