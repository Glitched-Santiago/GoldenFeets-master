package com.app.GoldenFeets.Config;

import com.app.GoldenFeets.Entity.Administrador;
import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate; // <-- Import necesario para la fecha de nacimiento

/**
 * Componente que se ejecuta al iniciar la aplicación para inicializar datos de prueba.
 * Implementa CommandLineRunner, lo que garantiza que el método run() se ejecute
 * una vez que el contexto de la aplicación esté cargado.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Iniciando la inicialización de datos...");

        // --- Usuario Administrador de Ejemplo ---
        if (usuarioRepository.findByEmail("admin@goldenf.com").isEmpty()) {
            Administrador admin = new Administrador();
            admin.setEmail("admin@goldenf.com");
            // CAMPOS ACTUALIZADOS
            admin.setNombres("Admin");
            admin.setApellidos("General");
            admin.setPassword(passwordEncoder.encode("admin123"));
            // Los campos numeroDocumento y fechaNacimiento pueden ser null para el admin
            usuarioRepository.save(admin);
            System.out.println("✅ Usuario Administrador de prueba creado.");
        }

        // --- Usuario Cliente de Ejemplo ---
        if (usuarioRepository.findByEmail("cliente@email.com").isEmpty()) {
            Cliente cliente = new Cliente();
            cliente.setEmail("cliente@email.com");
            // CAMPOS ACTUALIZADOS
            cliente.setNombres("Carlos");
            cliente.setApellidos("Cliente");
            cliente.setPassword(passwordEncoder.encode("cliente123"));
            // NUEVOS CAMPOS AÑADIDOS
            cliente.setNumeroDocumento("12345678X");
            cliente.setFechaNacimiento(LocalDate.of(1995, 5, 20)); // Año, Mes, Día
            usuarioRepository.save(cliente);
            System.out.println("✅ Usuario Cliente de prueba creado.");
        }

        System.out.println("Finalizada la inicialización de datos.");
    }
}