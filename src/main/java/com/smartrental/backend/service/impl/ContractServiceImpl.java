package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.ContractCreateDTO;
import com.smartrental.backend.dto.response.ContractResponseDTO;
import com.smartrental.backend.dto.response.LandlordCustomerDTO;
import com.smartrental.backend.entity.*;
import com.smartrental.backend.mapper.ContractMapper;
import com.smartrental.backend.repository.AppointmentRepository;
import com.smartrental.backend.repository.ContractRepository;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.ContractService;
import com.smartrental.backend.service.impl.NotificationServiceImpl; // Đảm bảo import đúng
import com.smartrental.backend.entity.NotificationType; // Nếu bạn dùng Enum này

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
                .status(Contract.Status.ACTIVE)
                .build();

        Contract savedContract = contractRepository.save(contract);

        // 5. Cập nhật trạng thái phòng
        boolean isRoomFull = false;
        if (room.getRentalType() == Room.RentalType.WHOLE) {
            room.setStatus(Room.Status.FULL);
            room.setCurrentTenants(1);
            isRoomFull = true;
        } else {
            int currentCount = (room.getCurrentTenants() == null) ? 0 : room.getCurrentTenants();
            int newCount = currentCount + 1;
            room.setCurrentTenants(newCount);
            if (newCount >= room.getCapacity()) {
                room.setStatus(Room.Status.FULL);
                isRoomFull = true;
            }
        }
        roomRepository.save(room);

        // 6. [TRIGGER] TỰ ĐỘNG HỦY CÁC LỊCH HẸN KHÁC NẾU PHÒNG FULL
        if (isRoomFull) {
            List<Appointment> pendingAppointments = appointmentRepository.findByRoom_IdAndStatusIn(
                    room.getId(),
                    Arrays.asList(Appointment.Status.PENDING, Appointment.Status.CONFIRMED)
            );

            for (Appointment appt : pendingAppointments) {
                if (!appt.getTenant().getId().equals(tenant.getId())) {
                    appt.setStatus(Appointment.Status.CANCELLED);
                    String cancelMessage = "Rất tiếc, phòng '" + room.getTitle() + "' đã đủ người thuê. Lịch hẹn của bạn đã tự động bị hủy.";
                    notificationService.sendNotification(
                            appt.getTenant(),
                            "❌ Phòng đã hết chỗ",
                            cancelMessage,
                            NotificationType.SYSTEM,
                            appt.getId()
                    );
                } else {
                    appt.setStatus(Appointment.Status.COMPLETED);
                }
            }
            appointmentRepository.saveAll(pendingAppointments);
        }

        // 7. GỬI THÔNG BÁO
        String message = "Chủ trọ đã tạo hợp đồng thuê phòng '" + room.getTitle() + "' cho bạn. Vui lòng kiểm tra mục Hợp đồng.";
        notificationService.sendNotification(
                tenant,
                "✅ Hợp đồng thuê phòng mới",
                message,
                NotificationType.CONTRACT_SIGN,
                savedContract.getId()
        );

        return contractMapper.toResponse(savedContract);
    }

    // ===> ĐÂY LÀ HÀM DUY NHẤT (Đã gộp Active + Potential) <===
    @Override
    public List<LandlordCustomerDTO> getCustomersByLandlord() {
        // 1. Lấy ID chủ trọ hiện tại
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User landlord = userRepository.findByEmail(email).orElseThrow();

        List<LandlordCustomerDTO> result = new ArrayList<>();

        // =========================================================
        // PHẦN 1: KHÁCH ĐANG THUÊ (ACTIVE) - Giữ nguyên
        // =========================================================
        List<Contract> contracts = contractRepository.findByLandlordId(landlord.getId());

        List<LandlordCustomerDTO> contractCustomers = contracts.stream().map(c -> LandlordCustomerDTO.builder()
                .contractId(c.getId())
                .tenantId(c.getTenant().getId())
                .tenantName(c.getTenant().getFullName())
                .tenantPhone(c.getTenant().getPhone())
                .tenantAvatar(c.getTenant().getAvatarUrl())
                .roomTitle(c.getRoom().getTitle())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .status("ACTIVE")
                .build()
        ).toList();

        result.addAll(contractCustomers);

        // =========================================================
        // PHẦN 2: KHÁCH QUAN TÂM (POTENTIAL) - CẬP NHẬT LỌC BỎ HỦY
        // =========================================================
        List<Appointment> appointments = appointmentRepository.findByRoom_LandlordIdOrderByCreatedAtDesc(landlord.getId());

        List<LandlordCustomerDTO> potentialCustomers = appointments.stream()
                // 1. Lọc trùng: Nếu khách này đã có trong danh sách Hợp đồng rồi thì thôi
                .filter(a -> contracts.stream().noneMatch(c -> c.getTenant().getId().equals(a.getTenant().getId())))

                // 2. [MỚI] Lọc bỏ những lịch hẹn đã bị HỦY (CANCELLED)
                .filter(a -> a.getStatus() != Appointment.Status.CANCELLED)

                .map(a -> LandlordCustomerDTO.builder()
                        .contractId(null)
                        .tenantId(a.getTenant().getId())
                        .tenantName(a.getTenant().getFullName())
                        .tenantPhone(a.getTenant().getPhone())
                        .tenantAvatar(a.getTenant().getAvatarUrl())
                        .roomTitle(a.getRoom().getTitle())
                        .startDate(null)
                        .endDate(null)
                        .status("POTENTIAL")
                        .build()
                ).toList();

        result.addAll(potentialCustomers);

        return result;
    }
}