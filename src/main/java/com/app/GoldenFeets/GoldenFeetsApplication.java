package com.app.GoldenFeets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // Importa la clase

@SpringBootApplication
@EnableAsync // Habilita la ejecución de métodos asíncronos
public class GoldenFeetsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoldenFeetsApplication.class, args);
    }

}