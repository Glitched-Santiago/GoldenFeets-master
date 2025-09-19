package com.app.GoldenFeets.DTO;

import lombok.Data;

// Aquí es donde añadirías las anotaciones de validación, por ejemplo:
// import jakarta.validation.constraints.*;

@Data
public class RegistroUsuarioDTO {

    // @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    // @NotBlank(message = "El email no puede estar vacío")
    // @Email(message = "El formato del email no es válido")
    private String email;

    // @NotBlank(message = "La contraseña no puede estar vacía")
    // @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    // Nota: El rol no se incluye aquí, ya que normalmente un usuario que se registra
    // desde un formulario público siempre será un "CLIENTE". El rol se asignaría
    // directamente en la lógica del servicio.

}