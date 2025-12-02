package com.app.GoldenFeets.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "rol", discriminatorType = DiscriminatorType.STRING)
@Data // Mantenemos Lombok para el resto, pero sobreescribimos fotoUrl
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDate fechaCreacion;

    @Column(name = "numero_documento", length = 20)
    private String numeroDocumento;

    @Column(name = "fecha_nacimiento")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    // --- CAMPO PROBLEMÁTICO ---
    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDate.now();
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    // --- AGREGA ESTO MANUALMENTE PARA ARREGLAR EL ERROR 500 ---
    // Esto asegura que Thymeleaf pueda leer la propiedad aunque Lombok falle
    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

}