package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.AppointmentCreateDTO;
import com.smartrental.backend.dto.response.AppointmentResponseDTO;
import com.smartrental.backend.entity.*;
import com.smartrental.backend.mapper.AppointmentMapper;
import com.smartrental.backend.repository.AppointmentRepository;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.AppointmentService;
import com.smartrental.backend.service.NotificationService; // 1. Import Interface
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final AppointmentMapper appointmentMapper;

    // 2. Sửa Injection: Dùng Interface thay vì Class Impl
    private final NotificationService notificationService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentCreateDTO dto) {
        User currentUser = getCurrentUser(); // Người đang thực hiện thao tác
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        User tenant;

        // 🟢 LOGIC MỚI: Kiểm tra ai đang tạo lịch
        // Nếu người đăng nhập là chủ trọ của phòng này -> Họ đang tạo lịch hộ khách hàng
        if (room.getLandlord().getId().equals(currentUser.getId())) {
            if (dto.getTenantId() == null) {
                throw new RuntimeException("Chủ trọ tạo lịch cần cung cấp ID khách hàng (tenantId)!");
            }
            tenant = userRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        } else {
            // Nếu người đăng nhập không phải chủ trọ -> Họ chính là khách hàng đi thuê
            tenant = currentUser;
        }

        // Kiểm tra: Không cho phép chủ trọ tự đặt lịch cho chính mình (dù là tự đặt hay nhập ID của mình)
        if (room.getLandlord().getId().equals(tenant.getId())) {
            throw new RuntimeException("Bạn không thể đặt lịch xem phòng của chính mình!");
        }

        if (room.getStatus() != Room.Status.ACTIVE) {
            throw new RuntimeException("Phòng này không khả dụng để đặt lịch!");
        }

        Appointment appointment = Appointment.builder()
                .tenant(tenant)
                .room(room)
                .meetTime(dto.getMeetTime())
                .message(dto.getMessage())
                .status(Appointment.Status.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // 🟢 CẬP NHẬT THÔNG BÁO: Gửi cho người "đối diện"
        User receiver = room.getLandlord().getId().equals(currentUser.getId()) ? tenant : room.getLandlord();
        String notifyTitle = room.getLandlord().getId().equals(currentUser.getId()) ? "📅 Lịch hẹn mới từ chủ trọ" : "📅 Yêu cầu xem phòng mới";

        notificationService.sendNotification(
                receiver,
                notifyTitle,
                String.format("Lịch xem phòng '%s'. Lời nhắn: %s", room.getTitle(), dto.getMessage()),
                NotificationType.SYSTEM,
                saved.getId()
        );

        return appointmentMapper.toResponse(saved, currentUser.getId());
    }

    @Override
    @Transactional
    public AppointmentResponseDTO updateStatus(Long id, String statusStr) {
        User currentUser = getCurrentUser();
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch hẹn không tồn tại"));

        Appointment.Status newStatus;
        try {
            newStatus = Appointment.Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ!");
        }

        boolean isLandlord = appointment.getRoom().getLandlord().getId().equals(currentUser.getId());
        boolean isTenant = appointment.getTenant().getId().equals(currentUser.getId());

        if (isLandlord) {
            if (newStatus == Appointment.Status.CONFIRMED) {
                appointment.setStatus(Appointment.Status.CONFIRMED);
                notificationService.sendNotification(
                        appointment.getTenant(),
                        "✅ Lịch xem phòng được xác nhận",
                        "Chủ trọ đã đồng ý lịch xem phòng: " + appointment.getRoom().getTitle(),
                        NotificationType.SYSTEM,
                        appointment.getId()
                );
            } else if (newStatus == Appointment.Status.CANCELLED) {
                appointment.setStatus(Appointment.Status.CANCELLED);
                notificationService.sendNotification(
                        appointment.getTenant(),
                        "❌ Lịch xem phòng bị từ chối",
                        "Chủ trọ bận hoặc đã cho thuê phòng: " + appointment.getRoom().getTitle(),
                        NotificationType.SYSTEM,
                        appointment.getId()
                );
            } else {
                throw new RuntimeException("Chủ trọ chỉ được DUYỆT hoặc TỪ CHỐI.");
            }
        } else if (isTenant) {
            if (newStatus == Appointment.Status.CANCELLED) {
                appointment.setStatus(Appointment.Status.CANCELLED);
                notificationService.sendNotification(
                        appointment.getRoom().getLandlord(),
                        "⚠️ Khách hủy lịch xem phòng",
                        "Khách " + currentUser.getFullName() + " đã hủy lịch xem phòng " + appointment.getRoom().getTitle(),
                        NotificationType.SYSTEM,
                        appointment.getId()
                );
            } else {
                throw new RuntimeException("Khách thuê chỉ được phép HỦY yêu cầu.");
            }
        } else {
            throw new RuntimeException("Bạn không có quyền thao tác trên lịch hẹn này.");
        }

        return appointmentMapper.toResponse(appointmentRepository.save(appointment), currentUser.getId());
    }

    @Override
    public List<AppointmentResponseDTO> getMyAppointments() {
        User currentUser = getCurrentUser();
        List<AppointmentResponseDTO> result = new ArrayList<>();

        List<Appointment> myRequests = appointmentRepository.findByTenantIdOrderByCreatedAtDesc(currentUser.getId());
        result.addAll(myRequests.stream().map(a -> appointmentMapper.toResponse(a, currentUser.getId())).toList());

        List<Appointment> incomingRequests = appointmentRepository.findByRoom_LandlordIdOrderByCreatedAtDesc(currentUser.getId());
        result.addAll(incomingRequests.stream().map(a -> appointmentMapper.toResponse(a, currentUser.getId())).toList());

        result.sort(Comparator.comparing(AppointmentResponseDTO::getCreatedAt).reversed());
        return result;
    }

    @Override
    @Transactional
    public void cancelAllAppointmentsForRoom(Long roomId, String reason) {
        List<Appointment> activeAppointments = appointmentRepository.findByRoom_IdAndStatusIn(
                roomId,
                List.of(Appointment.Status.PENDING, Appointment.Status.CONFIRMED)
        );

        if (activeAppointments.isEmpty()) return;

        for (Appointment app : activeAppointments) {
            app.setStatus(Appointment.Status.CANCELLED);
            notificationService.sendNotification(
                    app.getTenant(),
                    "❌ Phòng đã được cho thuê",
                    "Rất tiếc, phòng '" + app.getRoom().getTitle() + "' đã được ký hợp đồng với người khác. " + reason,
                    NotificationType.SYSTEM,
                    app.getId()
            );
        }
        appointmentRepository.saveAll(activeAppointments);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO suggestNewTime(Long id, LocalDateTime newTime, String note) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch hẹn không tồn tại"));

        User currentUser = getCurrentUser();

        if (!appt.getRoom().getLandlord().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền đề xuất giờ cho phòng này");
        }

        appt.setSuggestedMeetTime(newTime);
        appt.setStatus(Appointment.Status.SUGGESTED);
        String oldMsg = appt.getMessage() == null ? "" : appt.getMessage();
        appt.setMessage(oldMsg + " | [Chủ đề xuất]: " + note);

        Appointment saved = appointmentRepository.save(appt);

        notificationService.sendNotification(
                appt.getTenant(),
                "⏳ Chủ trọ đề xuất giờ xem mới",
                "Chủ trọ muốn dời lịch sang: " + newTime.toString().replace("T", " ") + ". Bấm để xác nhận ngay.",
                NotificationType.APPOINTMENT_SUGGESTION,
                saved.getId()
        );

        return appointmentMapper.toResponse(saved, currentUser.getId());
    }

    // =====================================================================
    //  3. SỬA HÀM NÀY ĐỂ FIX LỖI F5
    // =====================================================================
    @Override
    @Transactional
    public AppointmentResponseDTO acceptSuggestion(Long id) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch hẹn không tồn tại"));

        User currentUser = getCurrentUser();

        if (!appt.getTenant().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chấp nhận lịch này");
        }

        // --- XỬ LÝ IDEMPOTENCY ---
        // Nếu đã Confirm rồi, vẫn gọi hàm markAsRead để đảm bảo DB cập nhật trạng thái đọc
        if (appt.getStatus() == Appointment.Status.CONFIRMED) {
            notificationService.markAsReadByReference(appt.getId(), NotificationType.APPOINTMENT_SUGGESTION);
            return appointmentMapper.toResponse(appt, currentUser.getId());
        }

        if (appt.getStatus() != Appointment.Status.SUGGESTED) {
            throw new RuntimeException("Lịch hẹn không ở trạng thái chờ xác nhận đổi giờ (Trạng thái hiện tại: " + appt.getStatus() + ")");
        }

        // Logic cập nhật
        appt.setMeetTime(appt.getSuggestedMeetTime());
        appt.setSuggestedMeetTime(null);
        appt.setStatus(Appointment.Status.CONFIRMED);

        Appointment saved = appointmentRepository.save(appt);

        // --- GỌI HÀM NÀY ĐỂ FIX LỖI F5 ---
        // Đánh dấu thông báo đề xuất là ĐÃ ĐỌC ngay lập tức
        notificationService.markAsReadByReference(saved.getId(), NotificationType.APPOINTMENT_SUGGESTION);

        // Gửi thông báo cho chủ trọ
        notificationService.sendNotification(
                appt.getRoom().getLandlord(),
                "✅ Khách đã chốt lịch mới",
                "Khách thuê đã đồng ý với giờ bạn đề xuất. Lịch hẹn đã được cập nhật.",
                NotificationType.SYSTEM,
                saved.getId()
        );

        return appointmentMapper.toResponse(saved, currentUser.getId());
    }
}