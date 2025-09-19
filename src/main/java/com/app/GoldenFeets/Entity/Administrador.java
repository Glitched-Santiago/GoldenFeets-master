package com.app.GoldenFeets.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("ADMIN")
@Data
@EqualsAndHashCode(callSuper = true)
public class Administrador extends Usuario {

    // El cuerpo también puede estar vacío.
    // Si un administrador necesitara campos extra (ej: nivel de permisos), irían aquí.

}