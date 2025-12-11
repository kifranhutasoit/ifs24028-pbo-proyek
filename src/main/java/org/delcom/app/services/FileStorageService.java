package org.delcom.app.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    
    @Value("${app.upload.dir:./uploads}") 
    protected String uploadDir;

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String storeFile(MultipartFile file, UUID tugasId) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        // 1. Logic Create Directory (Akan di-cover test khusus)
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        
        // 2. Logic Filename (Akan di-cover test Null & No Extension)
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = "barang" + tugasId.toString() + fileExtension;

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    public boolean deleteFile(String filename) {
        if (filename == null || filename.isEmpty()) return false;
        
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            // Panggil method protected helper
            return deleteFileInternal(filePath);
        } catch (IOException e) {
            // 3. Catch Block (Akan di-cover dengan Mockito Spy)
            return false;
        }
    }

    // METHOD BARU: Protected agar bisa di-mock oleh Test
    protected boolean deleteFileInternal(Path path) throws IOException {
        return Files.deleteIfExists(path);
    }
}