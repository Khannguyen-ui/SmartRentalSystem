package com.smartrental.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.smartrental.backend.dto.request.RoomCreateDTO;
import com.smartrental.backend.dto.request.RoomUpdateDTO;
import com.smartrental.backend.dto.response.RoomResponseDTO;

public interface RoomService {


    RoomResponseDTO createRoom(RoomCreateDTO dto);


    Page<RoomResponseDTO> searchNearby(double lat, double lng, double radius, String keyword, Pageable pageable);


    List<RoomResponseDTO> getMyRooms();


    void deleteRoom(Long id);


    Page<RoomResponseDTO> searchNearbyAdvanced(
            Double lat, Double lng, Double radius,
            String keyword, String type,
            BigDecimal minPrice, BigDecimal maxPrice,
            Double minArea, Double maxArea,
            List<Integer> bedroomList, List<Integer> bathroomList,
            List<String> directionList, String furniture,
            Pageable pageable
    );

    RoomResponseDTO getRoomDetail(Long id);

    RoomResponseDTO updateRoom(Long id, RoomUpdateDTO dto);

    List<RoomResponseDTO> getRoomsByLandlord(Long landlordId);

    Page<RoomResponseDTO> getRoomsWithVideo(Pageable pageable);



    void pushRoomToTop(Long roomId, Long packageId);

    void toggleAutoRenew(Long roomId, boolean enable);

    void updateRoomStatus(Long roomId, String status);
}