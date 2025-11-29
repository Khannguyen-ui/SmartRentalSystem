package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // Đếm số hợp đồng đang Active của 1 phòng (Dùng để check khóa phòng Nguyên căn)
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.room.id = :roomId AND c.status = 'ACTIVE'")
    long countActiveContractsByRoom(Long roomId);
}