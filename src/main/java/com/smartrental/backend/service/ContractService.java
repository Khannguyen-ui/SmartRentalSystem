package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.ContractCreateDTO;
import com.smartrental.backend.dto.response.ContractResponseDTO;
import com.smartrental.backend.dto.response.LandlordCustomerDTO;

import java.util.List;

public interface ContractService {
    ContractResponseDTO createContract(ContractCreateDTO dto);
    List<LandlordCustomerDTO> getCustomersByLandlord();
}