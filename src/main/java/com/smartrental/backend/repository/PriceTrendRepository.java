package com.smartrental.backend.repository;

import com.smartrental.backend.dto.response.PriceHistoryDTO;
import com.smartrental.backend.entity.PriceTrend;
import com.smartrental.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PriceTrendRepository extends JpaRepository<PriceTrend, Long> {

    // Tìm xu hướng giá trong bán kính 2km quanh tọa độ Point của phòng
    @Query("SELECT new com.smartrental.backend.dto.response.PriceHistoryDTO(" +
            "concat('T', t.month, '/', substr(cast(t.year as string), 3, 2)), " +
            "t.maxPrice, t.avgPrice, t.minPrice) " +
            "FROM PriceTrend t " +
            "WHERE function('ST_DWithin', t.areaCenter, :roomLocation, :radius) = true " +
            "AND t.rentalType = :type " +
            "ORDER BY t.year ASC, t.month ASC")
    List<PriceHistoryDTO> findNearbyTrends(
            @Param("roomLocation") org.locationtech.jts.geom.Point roomLocation,
            @Param("radius") double radiusInDegrees, // Thường là 0.02 (~2km)
            @Param("type") Room.RentalType type);
}