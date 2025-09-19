package com.app.GoldenFeets.DTO;

import lombok.Data;

@Data
public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String email;
    private String rol; // Campo para diferenciar entre "CLIENTE" y "ADMIN"

}