package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByStatus(Room.Status status);

    // --- CẬP NHẬT QUERY: Tìm kiếm theo Bán kính + Từ khóa (Title/Address) ---
    @Query(value = "SELECT * FROM rooms r " +
            "WHERE r.status = 'ACTIVE' " +
            "AND ST_DistanceSphere(r.location, ST_MakePoint(:longitude, :latitude)) <= :radius " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            nativeQuery = true)
    List<Room> findRoomsNearby(@Param("latitude") double latitude,
                               @Param("longitude") double longitude,
                               @Param("radius") double radiusInMeters,
                               @Param("keyword") String keyword); // <--- Thêm tham số này

    List<Room> findByLandlordId(Long landlordId);
    Optional<Room> findByIdAndLandlordId(Long id, Long landlordId);

    // Đếm tổng số phòng
    int countByLandlordId(Long landlordId);

    @Query("SELECT r.address FROM Room r WHERE r.landlord.id = :landlordId")
    List<String> findAddressesByLandlordId(@Param("landlordId") Long landlordId);
}