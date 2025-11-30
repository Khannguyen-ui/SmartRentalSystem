package com.smartrental.backend.mapper;

import com.smartrental.backend.dto.request.MarketItemDTO;
import com.smartrental.backend.entity.MarketItem;
import com.smartrental.backend.entity.User; // Import User
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MarketMapper {
    @Autowired private ModelMapper modelMapper;

    public MarketItem toEntity(MarketItemDTO dto) {
        return modelMapper.map(dto, MarketItem.class);
    }

    public MarketItemDTO toResponse(MarketItem entity) {
        MarketItemDTO dto = modelMapper.map(entity, MarketItemDTO.class);

        // Map thủ công thông tin người bán để tránh lỗi Proxy
        User seller = entity.getUser();
        if (seller != null) {
            dto.setSellerId(seller.getId());
            dto.setSellerName(seller.getFullName());
            dto.setSellerPhone(seller.getPhone());
        }
        return dto;
    }
}