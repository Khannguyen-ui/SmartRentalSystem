package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByStatus(Room.Status status);

    // --- CẬP NHẬT QUERY: Tìm kiếm theo Bán kính + Từ khóa (Title/Address) ---
    @Query(value = "SELECT * FROM rooms r " +
            "WHERE r.status = 'ACTIVE' " +
            "AND ST_DWithin(r.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radius) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "r.search_vector @@ plainto_tsquery('simple', lower(:keyword)))",
            // countQuery phải có phần WHERE giống hệt query chính
            countQuery = "SELECT count(*) FROM rooms r " +
                    "WHERE r.status = 'ACTIVE' " +
                    "AND ST_DWithin(r.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radius) " +
                    "AND (:keyword IS NULL OR :keyword = '' OR " +
                    "r.search_vector @@ plainto_tsquery('simple', lower(:keyword)))",
            nativeQuery = true)
    Page<Room> findRoomsNearby(@Param("latitude") double latitude,
                               @Param("longitude") double longitude,
                               @Param("radius") double radiusInMeters,
                               @Param("keyword") String keyword,
                               Pageable pageable);
    // 2. [MỚI BỔ SUNG] Tính toán thống kê giá quanh một tọa độ để làm Lịch sử giá
    // Phương thức này giúp Background Job lấy dữ liệu tổng hợp siêu nhanh
    @Query(value = "SELECT " +
            "  CAST(MIN(price) AS NUMERIC) as min_p, " +
            "  CAST(AVG(price) AS NUMERIC) as avg_p, " +
            "  CAST(MAX(price) AS NUMERIC) as max_p " +
            "FROM rooms " +
            "WHERE status = 'ACTIVE' " +
            "AND rental_type = :rentalType " +
            "AND ST_DistanceSphere(location, :centerPoint) <= :radius",
            nativeQuery = true)
    Map<String, Object> calculateStatsAroundPoint(
            @Param("centerPoint") Point centerPoint,
            @Param("radius") double radiusInMeters,
            @Param("rentalType") String rentalType);
    List<Room> findByLandlordId(Long landlordId);
    Optional<Room> findByIdAndLandlordId(Long id, Long landlordId);

    @Query(value = "SELECT * FROM rooms r " +
            "WHERE r.status = 'ACTIVE' " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM price_trends pt " +
            "  WHERE pt.month = :month " +
            "  AND pt.year = :year " +
            "  AND pt.rental_type = r.rental_type " +
            "  AND ST_DWithin(r.location, pt.area_center, 500)" + // Bán kính 500m
            ")", nativeQuery = true)
    List<Room> findRoomsNeedingTrendUpdate(@Param("month") int month, @Param("year") int year);

    // Đếm tổng số phòng
    int countByLandlordId(Long landlordId);

    @Query("SELECT r.address FROM Room r WHERE r.landlord.id = :landlordId")
    List<String> findAddressesByLandlordId(@Param("landlordId") Long landlordId);
}