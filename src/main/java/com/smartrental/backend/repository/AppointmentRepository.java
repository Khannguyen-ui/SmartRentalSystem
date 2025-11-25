package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // 1. Lấy danh sách lịch của Người thuê
    List<Appointment> findByTenantId(Long tenantId);

    // 2. Lấy danh sách lịch của Chủ trọ (Dựa trên phòng thuộc về chủ trọ)
    // Query này join bảng appointment -> room -> check landlord_id
    List<Appointment> findByRoom_LandlordId(Long landlordId);
}