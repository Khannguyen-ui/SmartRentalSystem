package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.AppointmentCreateDTO;
import com.smartrental.backend.dto.response.AppointmentResponseDTO;
import com.smartrental.backend.entity.Appointment;
import com.smartrental.backend.entity.Room;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.AppointmentMapper;
import com.smartrental.backend.repository.AppointmentRepository;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final AppointmentMapper appointmentMapper;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentCreateDTO dto) {
        User tenant = getCurrentUser();

        // 1. Tìm phòng
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // 2. Kiểm tra logic (Không được đặt phòng của chính mình)
        if (room.getLandlord().getId().equals(tenant.getId())) {
            throw new RuntimeException("Bạn không thể đặt lịch xem phòng của chính mình!");
        }
        if (room.getStatus() == Room.Status.FULL) {
            throw new RuntimeException("Phòng này hiện đã hết chỗ!");
        }
        // 3. Tạo Appointment
        Appointment appointment = new Appointment();
        appointment.setTenant(tenant);
        appointment.setRoom(room);
        appointment.setMeetTime(dto.getMeetTime());
        appointment.setMessage(dto.getMessage());
        appointment.setStatus(Appointment.Status.PENDING);

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentResponseDTO updateStatus(Long id, String statusStr) {
        User user = getCurrentUser();
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch hẹn không tồn tại"));

        // Chỉ Chủ trọ của phòng đó hoặc Khách đặt mới được quyền sửa
        boolean isLandlord = appointment.getRoom().getLandlord().getId().equals(user.getId());
        boolean isTenant = appointment.getTenant().getId().equals(user.getId());

        if (!isLandlord && !isTenant) {
            throw new RuntimeException("Bạn không có quyền thay đổi lịch hẹn này");
        }

        // Chuyển trạng thái
        try {
            Appointment.Status newStatus = Appointment.Status.valueOf(statusStr.toUpperCase());
            appointment.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + statusStr);
        }

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public List<AppointmentResponseDTO> getMyAppointments() {
        User user = getCurrentUser();
        List<Appointment> appointments;

        if (user.getRole() == User.Role.TENANT) {
            // Nếu là khách -> Lấy lịch mình đã đặt
            appointments = appointmentRepository.findByTenantId(user.getId());
        } else {
            // Nếu là chủ trọ -> Lấy lịch khách đặt phòng của mình
            appointments = appointmentRepository.findByRoom_LandlordId(user.getId());
        }

        return appointments.stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }
}