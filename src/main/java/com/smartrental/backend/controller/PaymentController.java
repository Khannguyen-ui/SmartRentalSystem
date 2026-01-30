package com.smartrental.backend.controller;

import com.smartrental.backend.entity.NotificationType;
import com.smartrental.backend.entity.Transaction;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.TransactionRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.NotificationService;
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
    private final NotificationService notificationService;

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
            try {
                String[] parts = orderInfo.split("_");
                Long userId = Long.parseLong(parts[3]);

                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    BigDecimal amount = new BigDecimal(totalPrice).divide(new BigDecimal(100));

                    if (!transactionRepository.existsByVnpayCode(transactionId)) {
                        // Cộng tiền vào ví [cite: 652]
                        user.setWalletBalance(user.getWalletBalance().add(amount));
                        userRepository.save(user);

                        // Lưu lịch sử giao dịch [cite: 638]
                        Transaction trans = transactionRepository.save(Transaction.builder()
                                .user(user)
                                .amount(amount)
                                .type("DEPOSIT")
                                .status("SUCCESS")
                                .vnpayCode(transactionId)
                                .createdAt(LocalDateTime.now())
                                .build());

                        // 2. GỬI THÔNG BÁO HỆ THỐNG
                        notificationService.sendNotification(
                                user,
                                "Nạp tiền thành công",
                                "Ví của bạn đã được cộng " + amount.toPlainString() + " VNĐ. Mã GD: " + transactionId,
                                NotificationType.BILL_NEW, // Loại tài chính
                                trans.getId() // Reference tới ID giao dịch [cite: 571]
                        );
                    }

                    response.sendRedirect("https://bdsforntend-production.up.railway.app" +
                            "/payment-success?status=success&amount="
                            + amount.toPlainString() + "&txnRef=" + transactionId);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect("https://bdsforntend-production.up.railway.app" +
                        "/payment-failed");
                return;
            }
        }
        response.sendRedirect("https://bdsforntend-production.up.railway.app" +
                "/payment-failed");
    }
}