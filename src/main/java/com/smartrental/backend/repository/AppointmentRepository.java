package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // 1. Lấy danh sách tôi đi xin thuê (Tenant)
    List<Appointment> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    // 2. Lấy danh sách người ta xin thuê phòng của tôi (Landlord)
    List<Appointment> findByRoom_LandlordIdOrderByCreatedAtDesc(Long landlordId);

    List<Appointment> findByRoom_IdAndStatusIn(Long roomId, List<Appointment.Status> statuses);

}