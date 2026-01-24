package com.smartrental.backend.repository;

import com.smartrental.backend.entity.PackageType;
import com.smartrental.backend.entity.ServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long> {
    // Tìm danh sách gói theo loại (MEMBERSHIP hoặc ROOM_PROMOTION)
    List<ServicePackage> findByType(PackageType type);

    // Tìm các gói đang hoạt động
    List<ServicePackage> findByActiveTrue();
}
