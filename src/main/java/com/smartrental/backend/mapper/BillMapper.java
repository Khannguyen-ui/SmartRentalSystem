package com.smartrental.backend.mapper;

import com.smartrental.backend.dto.request.BillCreateDTO;
import com.smartrental.backend.dto.response.BillResponseDTO;
import com.smartrental.backend.entity.Bill;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BillMapper {

    @Autowired
    private ModelMapper modelMapper;

    public Bill toEntity(BillCreateDTO dto) {
        return modelMapper.map(dto, Bill.class);
    }

    public BillResponseDTO toResponse(Bill entity) {
        BillResponseDTO dto = modelMapper.map(entity, BillResponseDTO.class);

        // Map thông tin ngữ cảnh để hiển thị trên App cho dễ hiểu
        if (entity.getContract() != null) {
            dto.setContractId(entity.getContract().getId());

            if (entity.getContract().getRoom() != null) {
                dto.setRoomTitle(entity.getContract().getRoom().getTitle());
            }

            if (entity.getContract().getTenant() != null) {
                dto.setTenantName(entity.getContract().getTenant().getFullName());
            }
        }

        return dto;
    }
}