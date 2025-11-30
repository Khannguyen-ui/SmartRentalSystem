package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Lấy lịch sử chat của 1 hội thoại
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}