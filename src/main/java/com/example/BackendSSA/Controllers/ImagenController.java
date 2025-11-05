package com.example.BackendSSA.Controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    
    // Inyecta la ruta de la carpeta de subida desde application.properties (./Upload/)
    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        // Verifica si el archivo está vacío
        if (file.isEmpty()) {
            return new ResponseEntity<>("Debe seleccionar un archivo.", HttpStatus.BAD_REQUEST);
        }

        try {
            // 1. Resolver la ruta absoluta y asegurar que la carpeta 'Upload' existe
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 2. Generar un nombre de archivo único
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                 extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            Path filePath = uploadPath.resolve(uniqueFilename);
            
            // 3. Guardar el archivo en el sistema de archivos
            Files.copy(file.getInputStream(), filePath);

            // 4. Devolver la URL pública (ej: /uploads/asdf-123.jpg)
            String fileUrl = "/uploads/" + uniqueFilename;
            
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", fileUrl);
            
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error al subir la imagen: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error desconocido: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}
