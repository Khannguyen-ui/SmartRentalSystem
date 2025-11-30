package com.smartrental.backend.repository;

import com.smartrental.backend.entity.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {

    // 1. Lấy lịch sử đặt dịch vụ của một người thuê (Tenant)
    List<ServiceOrder> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    // 2. Lấy danh sách đơn hàng của một Đối tác (Để đối tác vào xem có ai đặt không)
    List<ServiceOrder> findByPartnerIdOrderByBookingTimeDesc(Integer partnerId);

    // 3. Tìm các đơn hàng theo trạng thái (VD: PENDING để Admin xử lý)
    List<ServiceOrder> findByStatus(String status);
}