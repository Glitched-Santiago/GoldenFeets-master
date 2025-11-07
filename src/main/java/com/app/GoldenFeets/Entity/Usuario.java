package com.app.GoldenFeets.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "rol", discriminatorType = DiscriminatorType.STRING)
@Data
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

    // --- CAMPOS MOVIDOS A LA CLASE PADRE ---
    // Estos campos serán null para los usuarios que no los necesiten (como un admin por defecto)
    @Column(name = "numero_documento", length = 20)
    private String numeroDocumento;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDate.now();
    }
}