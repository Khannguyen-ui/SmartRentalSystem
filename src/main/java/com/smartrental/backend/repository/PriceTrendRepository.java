package com.smartrental.backend.repository;

import com.smartrental.backend.dto.response.PriceHistoryDTO;
import com.smartrental.backend.entity.PriceTrend;
import com.smartrental.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.locationtech.jts.geom.Point; // Đảm bảo import đúng Point từ JTS
import java.util.List;

@Repository
public interface PriceTrendRepository extends JpaRepository<PriceTrend, Long> {

    @Query("SELECT new com.smartrental.backend.dto.response.PriceHistoryDTO(" +
            "concat('T', t.month, '/', substr(cast(t.year as string), 3, 2)), " +
            "MAX(t.maxPrice), " +
            "AVG(t.avgPrice), " +
            "MIN(t.minPrice)) " +
            "FROM PriceTrend t " +
            "WHERE function('ST_DWithin', t.areaCenter, :roomLocation, :radius) = true " +
            "AND t.rentalType = :type " +
            "GROUP BY t.month, t.year " +
            "ORDER BY t.year ASC, t.month ASC")
    List<PriceHistoryDTO> findNearbyTrends(
            @Param("roomLocation") Point roomLocation,
            @Param("radius") double radius,
            @Param("type") String type   // ✅ STRING
    );

    @Query(value = "SELECT EXISTS(SELECT 1 FROM price_trends " +
            "WHERE month = :month AND year = :year AND rental_type = :type " +
            "AND ST_DWithin(area_center, :point, 500))", nativeQuery = true)
    boolean existsNearby(@Param("month") int month,
                         @Param("year") int year,
                         @Param("type") String type,
                         @Param("point") Point point);
}
