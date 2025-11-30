package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    // Tìm sự cố của 1 hợp đồng
    List<Issue> findByContractId(Long contractId);

    // Tìm sự cố của 1 phòng (Để chủ trọ xem)
    List<Issue> findByContract_RoomId(Long roomId);
}