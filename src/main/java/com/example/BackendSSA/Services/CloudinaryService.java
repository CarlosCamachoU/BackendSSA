package com.example.BackendSSA.Services;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.Cloudinary;

@Service
public class CloudinaryService {
    
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,              // opcional: carpeta lógica, ej. "productos"
                "resource_type", "image"
        ));

        // secure_url es el que usas en el frontend
        return (String) uploadResult.get("secure_url");
    }
}
