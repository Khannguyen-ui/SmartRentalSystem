package com.smartrental.backend.service;

import java.util.List;

import com.smartrental.backend.dto.request.RoomCreateDTO;
import com.smartrental.backend.dto.response.RoomResponseDTO;

public interface RoomService {

    /**
     * Tạo mới phòng (chủ trọ)
     * @param dto dữ liệu tạo phòng
     * @return thông tin phòng vừa tạo
     */
    RoomResponseDTO createRoom(RoomCreateDTO dto);

    /**
     * Tìm các phòng gần một tọa độ (lat/lng) trong bán kính radius (mét)
     * @param lat vĩ độ
     * @param lng kinh độ
     * @param radius bán kính tính bằng mét
     * @return danh sách RoomResponseDTO
     */
    List<RoomResponseDTO> searchNearby(double lat, double lng, double radius);

    /**
     * Lấy tất cả phòng của chủ trọ đang đăng nhập
     * @return danh sách RoomResponseDTO
     */
    List<RoomResponseDTO> getMyRooms();

    /**
     * Xoá phòng theo id (chỉ chủ trọ sở hữu mới được xoá)
     * @param id id phòng
     */
    void deleteRoom(Long id);
}
