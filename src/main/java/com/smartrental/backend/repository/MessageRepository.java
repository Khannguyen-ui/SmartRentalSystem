package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 1. Lấy lịch sử chat của 1 hội thoại
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    // 2. Đếm số tin nhắn chưa đọc (Dùng cho Sidebar)
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND m.isRead = false AND m.sender.id != :userId")
    long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    // 🟢 3. Tìm các tin nhắn chưa đọc để cập nhật trạng thái "Đã đọc"
    // Hàm này tìm: Tất cả tin trong hội thoại + Người gửi không phải tôi + Trạng thái chưa đọc
    List<Message> findByConversationIdAndSenderIdNotAndIsReadFalse(Long conversationId, Long userId);
}