package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.IssueCreateDTO;
import com.smartrental.backend.entity.Contract;
import com.smartrental.backend.entity.Issue;
import com.smartrental.backend.entity.NotificationType;
import com.smartrental.backend.repository.ContractRepository;
import com.smartrental.backend.repository.IssueRepository;
import com.smartrental.backend.service.impl.NotificationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueRepository issueRepository;
    private final ContractRepository contractRepository;

    // Inject NotificationService để gửi thông báo
    private final NotificationServiceImpl notificationService;

    // 1. Báo cáo sự cố (Tenant)
    @PostMapping
    public ResponseEntity<Issue> createIssue(@RequestBody @Valid IssueCreateDTO dto) {
        Contract contract = contractRepository.findById(dto.getContractId())
                .orElseThrow(() -> new RuntimeException("Hợp đồng không tồn tại"));

        Issue issue = Issue.builder()
                .contract(contract)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl()) // Link Cloudinary
                .status(Issue.Status.PENDING)
                .build();

        return ResponseEntity.ok(issueRepository.save(issue));
    }

    // 2. Cập nhật trạng thái (Landlord: PROCESSING -> DONE)
    @PutMapping("/{id}/status")
    public ResponseEntity<Issue> updateStatus(@PathVariable Long id, @RequestParam Issue.Status status) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sự cố không tồn tại"));

        issue.setStatus(status);
        Issue savedIssue = issueRepository.save(issue);

        // --- GỬI THÔNG BÁO CHO NGƯỜI THUÊ ---
        // Lấy thông tin Tenant từ Hợp đồng gắn với Sự cố
        if (savedIssue.getContract() != null && savedIssue.getContract().getTenant() != null) {
            String message = "Sự cố '" + savedIssue.getTitle() + "' đã được cập nhật trạng thái: " + status;

            notificationService.sendNotification(
                    savedIssue.getContract().getTenant(),
                    "Cập nhật tiến độ sửa chữa",
                    message,
                    NotificationType.ISSUE_UPDATE
            );
        }
        // --------------------------------------

        return ResponseEntity.ok(savedIssue);
    }

    // 3. Lấy danh sách sự cố theo Phòng (Cho Chủ trọ xem)
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Issue>> getIssuesByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(issueRepository.findByContract_RoomId(roomId));
    }
}