package com.smartrental.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartrental.backend.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // 1. Tìm kiếm bán kính (PostGIS Logic)
    // Tìm các phòng ACTIVE nằm trong bán kính :radius (mét)
    @Query(value = "SELECT * FROM rooms r " +
                   "WHERE ST_DWithin(r.location, " +
                   "ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), " +
                   ":radius) " +
                   "AND r.status = 'ACTIVE'", 
           nativeQuery = true)
    List<Room> findRoomsNearby(@Param("latitude") double latitude,
                               @Param("longitude") double longitude,
                               @Param("radius") double radiusInMeters);

    // 2. Tìm phòng của 1 chủ trọ (Để quản lý)
    List<Room> findByLandlordId(Long landlordId);

    // 3. Tìm phòng cụ thể của 1 chủ trọ (Để check quyền sửa/xóa)
    Optional<Room> findByIdAndLandlordId(Long id, Long landlordId);
}