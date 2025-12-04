package com.smartrental.backend.controller;

import com.smartrental.backend.entity.Transaction;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.TransactionRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    // 1. TẠO URL THANH TOÁN
    // Client gọi: POST /api/payment/create-payment?amount=50000&userId=1
    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(
            @RequestParam long amount,
            @RequestParam Long userId,
            HttpServletRequest request) {

        // Gói userId vào trong thông tin đơn hàng để khi VNPay trả về ta biết ai nạp
        // Format: NAP_TIEN_USER_{ID}
        String orderInfo = "NAP_TIEN_USER_" + userId;

        String paymentUrl = vnPayService.createPaymentUrl(amount, orderInfo, request);
        return ResponseEntity.ok(Map.of("url", paymentUrl));
    }

    // 2. XỬ LÝ KẾT QUẢ TRẢ VỀ (CALLBACK)
    // VNPay sẽ tự động gọi vào URL này sau khi khách thanh toán xong
    @GetMapping("/vnpay-return")
    public void paymentReturn(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int paymentStatus = vnPayService.orderReturn(request);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");

        if (paymentStatus == 1) {
            // --- LOGIC CỘNG TIỀN ---
            try {
                // Tách userId từ chuỗi "NAP_TIEN_USER_1"
                String[] parts = orderInfo.split("_");
                Long userId = Long.parseLong(parts[3]);

                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    BigDecimal amount = new BigDecimal(totalPrice).divide(new BigDecimal(100)); // Chia 100

                    // 1. Kiểm tra trùng lặp giao dịch (Tránh cộng tiền 2 lần)
                    if (!transactionRepository.existsByVnpayCode(transactionId)) {
                        // 2. Cộng tiền vào ví
                        user.setWalletBalance(user.getWalletBalance().add(amount));
                        userRepository.save(user);

                        // 3. Lưu lịch sử giao dịch
                        Transaction trans = Transaction.builder()
                                .user(user)
                                .amount(amount)
                                .type("DEPOSIT")
                                .status("SUCCESS")
                                .vnpayCode(transactionId)
                                .createdAt(LocalDateTime.now())
                                .build();
                        transactionRepository.save(trans);
                    }
                }
                // Redirect về trang thành công (Frontend ReactJS/Mobile)
                // Bạn có thể thay đổi link này thành deeplink của app: smartrental://payment-success
                response.sendRedirect("http://localhost:3000/payment-success");
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect("http://localhost:3000/payment-failed");
            }
        } else {
            // Redirect về trang thất bại
            response.sendRedirect("http://localhost:3000/payment-failed");
        }
    }
}