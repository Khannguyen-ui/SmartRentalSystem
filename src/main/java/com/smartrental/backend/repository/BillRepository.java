package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    // Tìm hóa đơn mới nhất của hợp đồng để lấy chỉ số điện/nước cũ
    Optional<Bill> findTopByContractIdOrderByCreatedAtDesc(Long contractId);
}