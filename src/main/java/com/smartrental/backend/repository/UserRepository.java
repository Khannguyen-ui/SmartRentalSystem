package com.smartrental.backend.repository;

import com.smartrental.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.smartrental.backend.dto.response.LandlordStatsDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

@Repository
public interface    UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query(value = "SELECT u.id, u.full_name, u.avatar_url, COUNT(r.id) as post_count " +
            "FROM users u " +
            "JOIN rooms r ON u.id = r.landlord_id " +
            "WHERE r.status = 'ACTIVE' " +
            "AND ST_DWithin(r.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radius / 111319) " + // 🟢 THÊM PHẦN CHIA NÀY
            "GROUP BY u.id, u.full_name, u.avatar_url " +
            "ORDER BY post_count DESC",
            nativeQuery = true)

    List<Object[]> findTopLandlordsNearbyRaw(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radius") double radius,
            Pageable pageable
    );


}