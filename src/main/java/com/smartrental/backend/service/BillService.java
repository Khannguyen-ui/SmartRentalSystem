package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.BillCreateDTO;
import com.smartrental.backend.dto.response.BillResponseDTO;

public interface BillService {
    BillResponseDTO createBill(BillCreateDTO dto);
}