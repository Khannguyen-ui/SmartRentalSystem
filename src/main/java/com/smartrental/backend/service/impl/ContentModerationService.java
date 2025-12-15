package com.smartrental.backend.service.impl; // Đã đổi thành package impl

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ContentModerationService {

    // Lấy Key từ application.properties
    @Value("${sightengine.user}")
    private String apiUser;

    @Value("${sightengine.secret}")
    private String apiSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. DUYỆT VĂN BẢN (Từ khóa cấm)
    private static final List<String> BAD_WORDS = Arrays.asList(
            "ma túy", "súng", "đạn", "cần sa", "thuốc lắc", "giết", "sex", "khiêu dâm", "bom", "mìn"
    );

    public boolean isTextSafe(String text) {
        if (text == null || text.isEmpty()) return true;
        String lowerText = text.toLowerCase();
        for (String word : BAD_WORDS) {
            if (lowerText.contains(word)) {
                System.out.println(">>> AI PHÁT HIỆN TỪ KHÓA CẤM: " + word);
                return false;
            }
        }
        return true;
    }

    // 2. DUYỆT ẢNH (GỌI API SIGHTENGINE)
    public boolean isImageSafe(String imageUrl) {
        try {
            // Gọi API kiểm tra các model: nudity, weapon, drugs, offensive
            String url = UriComponentsBuilder.fromHttpUrl("https://api.sightengine.com/1.0/check.json")
                    .queryParam("models", "nudity,weapon,drugs,offensive")
                    .queryParam("api_user", apiUser)
                    .queryParam("api_secret", apiSecret)
                    .queryParam("url", imageUrl)
                    .toUriString();

            // Gửi Request
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"success".equals(response.get("status"))) {
                System.out.println(">>> Lỗi gọi API AI (Cho qua): " + response);
                return true; // Nếu lỗi API hoặc hết lượt miễn phí thì tạm cho qua (Fail-open)
            }

            // --- PHÂN TÍCH KẾT QUẢ ---

            // 1. Check Vũ khí (Weapon)
            Map weapon = (Map) response.get("weapon");
            if (weapon != null && (double) weapon.get("probability") > 0.5) {
                System.out.println(">>> AI PHÁT HIỆN: VŨ KHÍ");
                return false;
            }

            // 2. Check Khỏa thân (Nudity)
            Map nudity = (Map) response.get("nudity");
            if (nudity != null) {
                // safe: độ an toàn (0 -> 1). Nếu < 0.2 tức là rất không an toàn.
                Object safeObj = nudity.get("safe");
                double safe = safeObj instanceof Integer ? (Integer) safeObj : (Double) safeObj;

                if (safe < 0.2) {
                    System.out.println(">>> AI PHÁT HIỆN: HÌNH ẢNH NHẠY CẢM");
                    return false;
                }
            }

            // 3. Check Ma túy/Rượu
            Map drugs = (Map) response.get("drugs");
            if (drugs != null && (double) drugs.get("probability") > 0.5) {
                System.out.println(">>> AI PHÁT HIỆN: CHẤT CẤM");
                return false;
            }

            System.out.println(">>> AI KẾT LUẬN: ẢNH AN TOÀN");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(">>> Exception khi gọi AI: " + e.getMessage());
            return true; // Lỗi mạng thì cho qua
        }
    }
}