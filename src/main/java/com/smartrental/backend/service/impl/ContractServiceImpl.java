package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.ContractCreateDTO;
import com.smartrental.backend.dto.response.ContractResponseDTO;
import com.smartrental.backend.entity.*;
import com.smartrental.backend.mapper.ContractMapper;
import com.smartrental.backend.repository.AppointmentRepository;
import com.smartrental.backend.repository.ContractRepository;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ContractMapper contractMapper;

    // Inject thêm Repo và Notification Service để xử lý Trigger
    private final AppointmentRepository appointmentRepository;
    private final NotificationServiceImpl notificationService;

    @Override
    @Transactional
    public ContractResponseDTO createContract(ContractCreateDTO dto) {
        // 1. Lấy thông tin phòng
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // 2. LOGIC HYBRID: Kiểm tra xem phòng còn trống không
        if (room.getRentalType() == Room.RentalType.WHOLE) {
            long activeContracts = contractRepository.countActiveContractsByRoom(room.getId());
            if (activeContracts > 0) {
                throw new RuntimeException("Phòng nguyên căn này đã có người thuê!");
            }
        } else {
            // Null safety cho trường hợp dữ liệu cũ chưa có currentTenants
            int currentCount = (room.getCurrentTenants() == null) ? 0 : room.getCurrentTenants();
            if (currentCount >= room.getCapacity()) {
                throw new RuntimeException("Phòng đã hết giường trống!");
            }
        }

        // 3. Lấy User người thuê
        User tenant = userRepository.findByEmail(dto.getTenantEmail())
                .orElseThrow(() -> new RuntimeException("Email người thuê chưa đăng ký hệ thống"));

        // 4. Tạo Entity Hợp đồng
        Contract contract = Contract.builder()
                .room(room)
                .tenant(tenant)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .monthlyRent(dto.getMonthlyRent())
                .depositAmount(dto.getDepositAmount())
                .electricPrice(dto.getElectricPrice())
                .waterPrice(dto.getWaterPrice())
                .serviceFees(dto.getServiceFees())
                .status(Contract.Status.ACTIVE) // Mặc định Active khi tạo
                .build();

        Contract savedContract = contractRepository.save(contract);

        // 5. Cập nhật trạng thái phòng (Tăng số người hoặc Khóa phòng)
        boolean isRoomFull = false; // Biến cờ để đánh dấu phòng đã đầy hay chưa

        if (room.getRentalType() == Room.RentalType.WHOLE) {
            room.setStatus(Room.Status.FULL);
            room.setCurrentTenants(1);
            isRoomFull = true; // Nguyên căn thì ký xong là Full luôn
        } else {
            int currentCount = (room.getCurrentTenants() == null) ? 0 : room.getCurrentTenants();
            int newCount = currentCount + 1;

            room.setCurrentTenants(newCount);

            // Nếu đã đủ người -> Chuyển trạng thái sang FULL
            if (newCount >= room.getCapacity()) {
                room.setStatus(Room.Status.FULL);
                isRoomFull = true;
            }
        }
        roomRepository.save(room);

        // =================================================================
        // 6. [TRIGGER] TỰ ĐỘNG HỦY CÁC LỊCH HẸN KHÁC NẾU PHÒNG FULL
        // =================================================================
        if (isRoomFull) {
            // A. Tìm tất cả các lịch hẹn đang chờ (PENDING) hoặc đã chốt lịch (CONFIRMED) của phòng này
            List<Appointment> pendingAppointments = appointmentRepository.findByRoom_IdAndStatusIn(
                    room.getId(),
                    Arrays.asList(Appointment.Status.PENDING, Appointment.Status.CONFIRMED)
            );

            for (Appointment appt : pendingAppointments) {
                // B. Chỉ hủy lịch của NGƯỜI KHÁC (Không hủy lịch của người vừa ký hợp đồng này)
                if (!appt.getTenant().getId().equals(tenant.getId())) {

                    // C. Cập nhật trạng thái sang CANCELLED
                    appt.setStatus(Appointment.Status.CANCELLED);

                    // D. Gửi thông báo chia buồn
                    String cancelMessage = "Rất tiếc, phòng '" + room.getTitle() + "' đã đủ người thuê hoặc đã được chốt. Lịch hẹn của bạn đã tự động bị hủy.";

                    notificationService.sendNotification(
                            appt.getTenant(),
                            "❌ Phòng đã hết chỗ",
                            cancelMessage,
                            NotificationType.SYSTEM,
                            appt.getId()
                    );
                } else {
                    // (Optional) Nếu là lịch của chính người thuê này -> Có thể chuyển sang COMPLETED
                    appt.setStatus(Appointment.Status.COMPLETED);
                }
            }
            // E. Lưu cập nhật hàng loạt
            appointmentRepository.saveAll(pendingAppointments);
        }
        // =================================================================

        // 7. GỬI THÔNG BÁO CHO NGƯỜI THUÊ (Xác nhận hợp đồng)
        String message = "Chủ trọ đã tạo hợp đồng thuê phòng '" + room.getTitle() + "' cho bạn. Vui lòng kiểm tra mục Hợp đồng.";
        notificationService.sendNotification(
                tenant,
                "✅ Hợp đồng thuê phòng mới",
                message,
                NotificationType.CONTRACT_SIGN,
                savedContract.getId()
        );

        // 8. Trả về DTO
        return contractMapper.toResponse(savedContract);
    }
}