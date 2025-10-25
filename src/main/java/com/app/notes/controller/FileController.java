package com.app.notes.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.notes.service.StorageService;

@RestController
@RequestMapping("/files")
public class FileController {

    private final StorageService storageService;

    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
        @RequestParam("file") MultipartFile file,
        Authentication auth
    ) {
        // Only admins can upload
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(Map.of("message", "Admins only"));
        }

        try {
            String fileUrl = storageService.uploadFile(file);
            return ResponseEntity.ok(Map.of(
                "message", "File uploaded successfully",
                "url", fileUrl,
                "fileName", file.getOriginalFilename()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "message", "Upload failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/url/{fileName}")
    public ResponseEntity<?> getFileUrl(@PathVariable String fileName) {
        try {
            String signedUrl = storageService.getSignedUrl(fileName);
            if (signedUrl == null) {
                return ResponseEntity.status(404).body(Map.of("message", "File not found"));
            }
            return ResponseEntity.ok(Map.of(
                "fileName", fileName,
                "url", signedUrl,
                "expiresIn", "1 hour"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "message", "Failed to generate URL: " + e.getMessage()
            ));
        }
    }
}
