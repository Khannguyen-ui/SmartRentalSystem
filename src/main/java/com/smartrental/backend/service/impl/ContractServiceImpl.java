package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.ContractCreateDTO;
import com.smartrental.backend.dto.response.ContractResponseDTO;
import com.smartrental.backend.entity.Contract;
import com.smartrental.backend.entity.NotificationType;
import com.smartrental.backend.entity.Room;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.ContractMapper;
import com.smartrental.backend.repository.ContractRepository;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ContractMapper contractMapper;

    // Inject Service thông báo
    private final NotificationServiceImpl notificationService;

    @Override
    @Transactional
    public ContractResponseDTO createContract(ContractCreateDTO dto) {
        // 1. Lấy thông tin phòng
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // 2. LOGIC HYBRID (Kiểm tra điều kiện thuê)
        if (room.getRentalType() == Room.RentalType.WHOLE) {
            // Nguyên căn: Nếu có HĐ Active -> Chặn
            long activeContracts = contractRepository.countActiveContractsByRoom(room.getId());
            if (activeContracts > 0) {
                throw new RuntimeException("Phòng nguyên căn này đã có người thuê!");
            }
        } else {
            // Ở ghép: Nếu full giường -> Chặn
            // Xử lý null safety cho currentTenants để tránh lỗi NullPointerException
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
                .status(Contract.Status.ACTIVE) // Demo set luôn Active (thực tế có thể để PENDING chờ ký)
                .build();

        Contract savedContract = contractRepository.save(contract);

        // 5. Cập nhật trạng thái phòng (Tăng số người hoặc Khóa phòng)
        if (room.getRentalType() == Room.RentalType.WHOLE) {
            room.setStatus(Room.Status.FULL);
            room.setCurrentTenants(1);
        } else {
            int currentCount = (room.getCurrentTenants() == null) ? 0 : room.getCurrentTenants();
            int newCount = currentCount + 1;

            room.setCurrentTenants(newCount);
            // Nếu đã đủ người -> Chuyển trạng thái sang FULL
            if (newCount >= room.getCapacity()) {
                room.setStatus(Room.Status.FULL);
            }
        }
        roomRepository.save(room);

        // 6. GỬI THÔNG BÁO CHO NGƯỜI THUÊ (LOGIC MỚI)
        // Bắn thông báo realtime xuống App của người thuê
        String message = "Chủ trọ đã tạo hợp đồng thuê phòng " + room.getTitle() + " cho bạn. Vui lòng kiểm tra và xác nhận.";

        notificationService.sendNotification(
                tenant,
                "Yêu cầu ký hợp đồng",
                message,
                NotificationType.CONTRACT_SIGN
        );

        // 7. Trả về DTO qua Mapper
        return contractMapper.toResponse(savedContract);
    }
}