package com.smartrental.backend.controller;

import com.smartrental.backend.service.impl.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    // API Upload 1 file (Ảnh hoặc Video)
    // POST /api/upload
    // Body: Form-data (Key="file", Value=[Chọn file])
    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn file!"));
            }

            String url = cloudinaryService.uploadFile(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", url); // Trả về: {"url": "https://..."}

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Upload thất bại: " + e.getMessage()));
        }
    }
}