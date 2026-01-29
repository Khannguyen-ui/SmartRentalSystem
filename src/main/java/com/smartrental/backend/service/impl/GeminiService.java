package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.gemini.GeminiRequest;
import com.smartrental.backend.dto.gemini.GeminiResponse;
import com.smartrental.backend.entity.Room;
import com.smartrental.backend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    private final RoomRepository roomRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public String getConsultation(String userQuestion) {

        // 1. Lấy phòng ACTIVE
        List<Room> activeRooms = roomRepository.findByStatus(Room.Status.ACTIVE);

        if (activeRooms.isEmpty()) {
            return "Hiện tại hệ thống chưa có phòng trống. Bạn quay lại sau nhé 🏠";
        }

        // ==================================================
        // 🔥 PHẦN QUAN TRỌNG: KHÔNG HIỂU CÂU HỎI – CHỈ ĐƯA DỮ LIỆU
        // ==================================================
        String q = userQuestion.toLowerCase();

        boolean isCompareQuestion =
                q.contains("rẻ")
                        || q.contains("nhất")
                        || q.contains("phù hợp")
                        || q.contains("tốt")
                        || q.contains("phòng ngủ")
                        || q.contains("wc")
                        || q.contains("diện tích")
                        || q.contains("khu vực")
                        || q.contains("chủ trọ");


        List<Room> contextRooms;

        if (isCompareQuestion) {
            // 👉 Câu hỏi cần suy luận → đưa nhiều dữ liệu cho AI
            contextRooms = activeRooms.stream()
                    .limit(15)   // đủ để AI so sánh
                    .toList();
        } else {
            // 👉 Câu hỏi bình thường
            contextRooms = activeRooms.stream()
                    .limit(5)
                    .toList();
        }

        // 2. Build prompt
        String prompt = buildPrompt(contextRooms, userQuestion);

        // 3. Build request body
        GeminiRequest request = new GeminiRequest(
                Collections.singletonList(
                        new GeminiRequest.Content(
                                Collections.singletonList(
                                        new GeminiRequest.Part(prompt)
                                )
                        )
                )
        );

        try {
            // 4. Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey.trim());

            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

            System.out.println(">>> GEMINI CALLING URL: " + apiUrl.trim());

            // 5. Call Gemini API
            GeminiResponse response = restTemplate.postForObject(
                    apiUrl.trim(),
                    entity,
                    GeminiResponse.class
            );

            if (response != null
                    && response.getCandidates() != null
                    && !response.getCandidates().isEmpty()
                    && response.getCandidates().get(0).getContent() != null
                    && !response.getCandidates().get(0).getContent().getParts().isEmpty()) {

                return response
                        .getCandidates()
                        .get(0)
                        .getContent()
                        .getParts()
                        .get(0)
                        .getText();
            }

            return "Mình chưa tìm được câu trả lời phù hợp. Bạn thử hỏi lại nhé 🙂";

        } catch (Exception e) {
            System.err.println(">>> GEMINI ERROR: " + e.getMessage());
            e.printStackTrace();
            return "Hệ thống tư vấn đang bận. Vui lòng thử lại sau.";
        }
    }

    // ==================================================
    // 🔥 PROMPT: AI TỰ SUY LUẬN – KHÔNG HARD-CODE
    // ==================================================
    private String buildPrompt(List<Room> rooms, String question) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
Bạn là trợ lý AI của hệ thống Smart Rental.
Nhiệm vụ của bạn là tư vấn phòng trọ dựa trên DỮ LIỆU THỰC TẾ bên dưới.

NGUYÊN TẮC BẮT BUỘC:
- CHỈ sử dụng thông tin trong danh sách phòng
- KHÔNG bịa phòng, giá, khu vực
- KHÔNG suy đoán ngoài dữ liệu
- Nếu không có phòng phù hợp → nói rõ là không có

KHẢ NĂNG SUY LUẬN:
- KHU VỰC: suy luận từ ĐỊA CHỈ (ví dụ: Thủ Đức, Quận 7, gần trường, gần KCN…)
- GIÁ: so sánh rẻ nhất / đắt nhất / trong khoảng
- XU HƯỚNG GIÁ: nếu nhiều phòng cùng khu vực có giá cao/thấp → nhận xét chung
- DIỆN TÍCH: lớn / nhỏ / trên – dưới X m²
- PHÒNG NGỦ / WC: nếu có trong mô tả hoặc tiện ích
- CHỦ TRỌ: nếu thông tin thể hiện là chính chủ, căn hộ, nhà trọ

CÁCH TRẢ LỜI:
- Ưu tiên phòng PHÙ HỢP NHẤT
- Có thể liệt kê 1–3 phòng
- Ngắn gọn, rõ ràng, thân thiện
- Emoji dùng vừa phải 🏠✨

DANH SÁCH PHÒNG:
""");

        for (Room r : rooms) {
            String price = (r.getPrice() != null) ? r.getPrice().toPlainString() : "Thương lượng";
            String amenities = (r.getAmenities() != null && !r.getAmenities().isEmpty())
                    ? String.join(", ", r.getAmenities())
                    : "Cơ bản";

            sb.append(String.format("""
[ID: %d] %s
- Giá: %s VNĐ
- Diện tích: %.1f m2
- Địa chỉ: %s
- Tiện ích / Mô tả: %s

""",
                    r.getId(),
                    r.getTitle(),
                    price,
                    r.getArea(),
                    r.getAddress(),
                    amenities
            ));
        }

        sb.append("""
---
CÂU HỎI KHÁCH HÀNG:
""");
        sb.append(question);

        return sb.toString();
    }

}
