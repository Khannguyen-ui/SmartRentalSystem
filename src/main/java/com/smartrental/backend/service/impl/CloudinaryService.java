package com.smartrental.backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        // resource_type: "auto" -> Tự động nhận diện là Ảnh hay Video
        // public_id: Tên file trên Cloud (để null thì nó tự sinh ngẫu nhiên)
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "auto"));

        // Trả về đường dẫn file trên mây (URL: https://res.cloudinary.com/...)
        return uploadResult.get("secure_url").toString();
    }
}