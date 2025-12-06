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
    // --------------------------------

    // Các hàm cũ giữ nguyên
    @Query(value = "SELECT * FROM rooms r " +
            "WHERE ST_DWithin(r.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radius) " +
            "AND r.status = 'ACTIVE'",
            nativeQuery = true)
    List<Room> findRoomsNearby(@Param("latitude") double latitude,
                               @Param("longitude") double longitude,
                               @Param("radius") double radiusInMeters);

    List<Room> findByLandlordId(Long landlordId);
    Optional<Room> findByIdAndLandlordId(Long id, Long landlordId);
}