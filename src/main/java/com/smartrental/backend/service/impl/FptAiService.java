package com.smartrental.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FptAiService {

    @Value("${fpt.ai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> scanIdCard(MultipartFile file) {
        // API Endpoint nhận diện CMND/CCCD của FPT
        String url = "https://api.fpt.ai/vision/idr/vnm";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("image", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseFptResponse(response.getBody());
            } else {
                throw new RuntimeException("Lỗi gọi FPT.AI: " + response.getStatusCode());
            }

        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file: " + e.getMessage());
        }
    }

    private Map<String, String> parseFptResponse(String jsonResponse) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            int errorCode = root.path("errorCode").asInt();

            if (errorCode != 0) {
                throw new RuntimeException("FPT AI không đọc được ảnh. Lỗi: " + root.path("errorMessage").asText());
            }

            JsonNode data = root.path("data").get(0);

            // Lấy dữ liệu (FPT trả về rất chi tiết, ta chỉ lấy số và tên)
            String id = data.path("id").path("content").asText();
            String name = data.path("name").path("content").asText();

            // Nếu đọc thất bại, FPT trả về "N/A", ta có thể check ở đây
            if ("N/A".equals(id) || "N/A".equals(name)) {
                throw new RuntimeException("Ảnh quá mờ, không đọc được thông tin.");
            }

            result.put("citizenId", id);
            result.put("fullName", name);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi phân tích dữ liệu: " + e.getMessage());
        }
        return result;
    }
}