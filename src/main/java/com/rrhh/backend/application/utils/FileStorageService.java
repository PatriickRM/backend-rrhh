package com.rrhh.backend.application.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final String uploadDir = "uploads/leave-evidence/";

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads");
        }
    }

    public String storeFile(MultipartFile file, Long employeeId) {
        if (file.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String fileName = employeeId + "_" + System.currentTimeMillis() + fileExtension;

            Path targetLocation = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;

        } catch (IOException ex) {
            throw new RuntimeException("Error al guardar archivo: " + ex.getMessage());
        }
    }

    public Resource loadFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Archivo no encontrado: " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error al cargar archivo: " + fileName);
        }
    }
}