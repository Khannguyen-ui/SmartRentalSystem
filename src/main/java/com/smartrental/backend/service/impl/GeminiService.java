package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.gemini.GeminiRequest;
import com.smartrental.backend.dto.gemini.GeminiResponse;
import com.smartrental.backend.entity.Room;
import com.smartrental.backend.entity.ServicePackage;
import com.smartrental.backend.entity.PriceTrend;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.ServicePackageRepository;
import com.smartrental.backend.repository.PriceTrendRepository;
import com.smartrental.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private final ServicePackageRepository packageRepository;
    private final PriceTrendRepository priceTrendRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public String getConsultation(String userQuestion) {

        List<Room> activeRooms = roomRepository.findByStatus(Room.Status.ACTIVE);
        List<ServicePackage> activePackages = packageRepository.findByActiveTrue();
        List<PriceTrend> priceTrends = priceTrendRepository.findAll();

        if (activeRooms.isEmpty() && activePackages.isEmpty()) {
            return "Hiện tại hệ thống chưa có dữ liệu phòng hoặc gói cước. Bạn quay lại sau nhé 🏠";
        }

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
                        || q.contains("chủ trọ")
                        || q.contains("gói")
                        || q.contains("đăng bài")
                        || q.contains("nạp tiền")
                        || q.contains("gần")
                        || q.contains("trường")
                        || q.contains("bệnh viện")
                        || q.contains("uy tín")
                        || q.contains("tin tưởng")
                        || q.contains("hợp lý");


        List<Room> contextRooms;

        if (isCompareQuestion) {
            contextRooms = activeRooms.stream()
                    .limit(10)
                    .toList();
        } else {
            contextRooms = activeRooms.stream()
                    .limit(5)
                    .toList();
        }

        String prompt = buildPrompt(contextRooms, activePackages, priceTrends, userQuestion);

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
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey.trim());

            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

            System.out.println(">>> GEMINI CALLING URL: " + apiUrl.trim());

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

    private String buildPrompt(List<Room> rooms, List<ServicePackage> packages, List<PriceTrend> trends, String question) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
Bạn là trợ lý AI của hệ thống Smart Rental. Trả lời đúng trọng tâm, ngắn gọn và TRỰC QUAN.
Nhiệm vụ của bạn là tư vấn dựa trên dữ liệu thực tế bên dưới.

QUY TẮC QUAN TRỌNG:
1. HÌNH ẢNH: Luôn hiển thị ảnh đại diện của phòng bằng cú pháp Markdown: ![Tên phòng](URL_Ảnh).
2. HIỂN THỊ: Gắn link Markdown cho phòng [Tên phòng](link) và chủ trọ [Tên chủ trọ](link).
3. TÌM KIẾM VỊ TRÍ: Phân tích địa chỉ/tọa độ để tìm phòng gần trường học, bệnh viện theo yêu cầu.
4. ĐÁNH GIÁ UY TÍN: Dựa vào số giao dịch thành công của chủ trọ để tư vấn mức độ tin tưởng.
5. ĐÁNH GIÁ GIÁ CẢ: So sánh giá phòng với dữ liệu "Xu hướng giá khu vực" để biết giá có hợp lý/tốt hay không.
6. BẢN ĐỒ: Luôn cung cấp link "Xem trên bản đồ" cho mỗi phòng.

DỮ LIỆU GIÁ THỊ TRƯỜNG (XU HƯỚNG GIÁ):
""");
        for (PriceTrend t : trends) {
            sb.append(String.format("- Khu vực %s: Trung bình %s VNĐ (Thấp nhất: %s, Cao nhất: %s)\n",
                    t.getAreaCenter(), t.getAvgPrice(), t.getMinPrice(), t.getMaxPrice()));
        }

        sb.append("\nDANH SÁCH GÓI DỊCH VỤ:\n");
        for (ServicePackage pkg : packages) {
            sb.append(String.format("- Gói %s: Giá %s VNĐ. [Xem chi tiết gói](https://smartrentalsystem-production.up.railway.app/packages/%d)\n",
                    pkg.getName(), pkg.getPrice().toPlainString(), pkg.getId()));
        }

        sb.append("\nDANH SÁCH PHÒNG HIỆN CÓ:\n");
        for (Room r : rooms) {
            String price = (r.getPrice() != null) ? r.getPrice().toPlainString() : "Thương lượng";
            long successDeals = transactionRepository.countByUserIdAndStatus(r.getLandlord().getId(), "SUCCESS");
            String encodedAddress = URLEncoder.encode(r.getAddress(), StandardCharsets.UTF_8);
            String googleMapsLink = "https://www.google.com/maps/search/?api=1&query=" + encodedAddress;

            // Lấy URL ảnh đầu tiên của phòng
            String imageUrl = (r.getImages() != null && !r.getImages().isEmpty())
                    ? r.getImages().get(0)
                    : "https://res.cloudinary.com/dfyrnocnr/image/upload/v1/default-room";

            sb.append(String.format("""
![%s](%s)
### **[%s](https://smartrentalsystem-production.up.railway.app/rooms/%d)**
- Giá: %s VNĐ | Diện tích: %.1f m2
- Địa chỉ: %s
- ĐỘ UY TÍN: Chủ trọ đã có %d giao dịch thành công.
- Bản đồ: [📍 Nhấn để xem vị trí trên Google Maps](%s)
- Chủ trọ: [%s](https://smartrentalsystem-production.up.railway.app/users/public-profile/%d)
---
""",
                    r.getTitle(), imageUrl,
                    r.getTitle(), r.getId(),
                    price, r.getArea(), r.getAddress(),
                    successDeals, googleMapsLink,
                    r.getLandlord().getFullName(), r.getLandlord().getId()));
        }

        sb.append("\n---\nCÂU HỎI KHÁCH HÀNG: ");
        sb.append(question);

        return sb.toString();
    }
}