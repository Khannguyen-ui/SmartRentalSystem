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
    // ------------------------------   --



    @Query(value = "SELECT * FROM rooms r " +
            "WHERE r.status = 'ACTIVE' " +
            "AND ST_DistanceSphere(r.location, ST_MakePoint(:longitude, :latitude)) <= :radius",
            nativeQuery = true)
    List<Room> findRoomsNearby(@Param("latitude") double latitude,
                               @Param("longitude") double longitude,
                               @Param("radius") double radiusInMeters);

    List<Room> findByLandlordId(Long landlordId);
    Optional<Room> findByIdAndLandlordId(Long id, Long landlordId);
}