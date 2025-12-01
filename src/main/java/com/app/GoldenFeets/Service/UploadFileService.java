package com.app.GoldenFeets.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UploadFileService {

    // Nombre de la carpeta donde se guardarán las imágenes (en la raíz del proyecto)
    private final String FOLDER = "images//";

    public String saveImage(MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            // 1. Convertimos a bytes
            byte[] bytes = file.getBytes();

            // 2. Generamos nombre único para evitar conflictos (ej: uuid_nombre.jpg)
            String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // 3. Ruta completa
            Path path = Paths.get(FOLDER + uniqueFilename);

            // 4. Escribimos el archivo
            Files.write(path, bytes);

            return uniqueFilename;
        }
        return "default.jpg"; // Imagen por defecto si falla
    }

    public void deleteImage(String nombre) {
        String ruta = "images//";
        try {
            Path path = Paths.get(ruta + nombre);
            if(Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}