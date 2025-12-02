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

    private final String FOLDER = "images"; // Sin barras, dejemos que Path se encargue

    public String saveImage(MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            // 1. Convertir bytes
            byte[] bytes = file.getBytes();

            // 2. Crear carpeta si no existe (Gestión de rutas segura)
            Path rootPath = Paths.get(FOLDER);
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
            }

            // 3. Generar nombre único y LIMPIO
            // Reemplazamos espacios por guiones para evitar problemas en URLs
            String originalName = file.getOriginalFilename();
            if (originalName != null) {
                originalName = originalName.replaceAll("\\s+", "-"); // Quita espacios
            }

            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalName;

            // 4. Resolver ruta completa de forma segura (funciona en Windows y Linux)
            Path completePath = rootPath.resolve(uniqueFilename);

            // 5. Escribir
            Files.write(completePath, bytes);

            return uniqueFilename;
        }
        return "default.jpg";
    }

    public void deleteImage(String nombre) {
        // Nunca borres la imagen por defecto
        if ("default.jpg".equals(nombre)) return;

        try {
            Path rootPath = Paths.get(FOLDER);
            Path path = rootPath.resolve(nombre);

            if(Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}