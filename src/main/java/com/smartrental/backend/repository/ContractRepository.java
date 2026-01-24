package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    boolean existsByTenantIdAndRoomId(Long tenantId, Long roomId);
    // Đếm số hợp đồng đang Active của 1 phòng (Dùng để check khóa phòng Nguyên căn)
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.room.id = :roomId AND c.status = 'ACTIVE'")
    long countActiveContractsByRoom(Long roomId);
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.room.landlord.id = :landlordId AND c.status = 'ACTIVE' OR c.status = 'ENDED'")
    int countSuccessfulDealsByLandlord(@Param("landlordId") Long landlordId);
    @Query("SELECT c FROM Contract c WHERE c.room.landlord.id = :landlordId ORDER BY c.createdAt DESC")
    List<Contract> findByLandlordId(@Param("landlordId") Long landlordId);
}