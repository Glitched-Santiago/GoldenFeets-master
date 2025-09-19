package com.app.GoldenFeets.Config;

import com.app.GoldenFeets.Entity.Administrador;
import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
        // Se comprueba si el usuario ya existe para no crearlo de nuevo en cada reinicio.
        if (usuarioRepository.findByEmail("admin@goldenf.com").isEmpty()) {
            Administrador admin = new Administrador();
            admin.setEmail("admin@goldenf.com");
            admin.setNombre("Admin General");
            // IMPORTANTE: La contraseña se encripta antes de guardarla.
            admin.setPassword(passwordEncoder.encode("admin123"));
            usuarioRepository.save(admin);
            System.out.println("✅ Usuario Administrador de prueba creado.");
        }

        // --- Usuario Cliente de Ejemplo ---
        if (usuarioRepository.findByEmail("cliente@email.com").isEmpty()) {
            Cliente cliente = new Cliente();
            cliente.setEmail("cliente@email.com");
            cliente.setNombre("Carlos Cliente");
            // IMPORTANTE: La contraseña se encripta antes de guardarla.
            cliente.setPassword(passwordEncoder.encode("cliente123"));
            usuarioRepository.save(cliente);
            System.out.println("✅ Usuario Cliente de prueba creado.");
        }

        System.out.println("Finalizada la inicialización de datos.");
    }
}