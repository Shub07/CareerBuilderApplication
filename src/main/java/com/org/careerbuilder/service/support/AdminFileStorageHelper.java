package com.org.careerbuilder.service.support;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class AdminFileStorageHelper {

    public String store(Long schoolId, String category, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        try {
            Path dir = Paths.get("uploads", "admin", category, String.valueOf(schoolId));
            Files.createDirectories(dir);
            String orig = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String safe = orig.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = dir.resolve(UUID.randomUUID() + "_" + safe);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString().replace("\\", "/");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store file: " + e.getMessage(), e);
        }
    }
}
