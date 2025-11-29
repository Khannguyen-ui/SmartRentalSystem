package com.smartrental.backend.mapper;

import com.smartrental.backend.dto.request.ContractCreateDTO;
import com.smartrental.backend.dto.response.ContractResponseDTO;
import com.smartrental.backend.entity.Contract;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    @Autowired
    private ModelMapper modelMapper;

    public Contract toEntity(ContractCreateDTO dto) {
        // Lưu ý: Việc map Room và Tenant sẽ được xử lý trong Service
        // vì cần query DB lấy object Room/User thực tế.
        // Ở đây chỉ map các field cơ bản.
        return modelMapper.map(dto, Contract.class);
    }

    public ContractResponseDTO toResponse(Contract entity) {
        ContractResponseDTO dto = modelMapper.map(entity, ContractResponseDTO.class);

        // Map thông tin phòng
        if (entity.getRoom() != null) {
            dto.setRoomId(entity.getRoom().getId());
            dto.setRoomTitle(entity.getRoom().getTitle());
            dto.setRoomAddress(entity.getRoom().getAddress());
        }

        // Map thông tin người thuê
        if (entity.getTenant() != null) {
            dto.setTenantId(entity.getTenant().getId());
            dto.setTenantName(entity.getTenant().getFullName());
            dto.setTenantEmail(entity.getTenant().getEmail());
            dto.setTenantPhone(entity.getTenant().getPhone());
        }

        return dto;
    }
}