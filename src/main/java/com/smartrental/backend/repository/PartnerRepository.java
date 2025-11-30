package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Integer> {

    // 1. Lấy danh sách đối tác đang hoạt động (Active = true)
    List<Partner> findByIsActiveTrue();

    // 2. Tìm đối tác theo loại dịch vụ (VD: chỉ lấy đội Chuyển nhà MOVING)
    List<Partner> findByServiceTypeAndIsActiveTrue(String serviceType);
}