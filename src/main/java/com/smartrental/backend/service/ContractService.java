package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.ContractCreateDTO;
import com.smartrental.backend.dto.response.ContractResponseDTO;

public interface ContractService {
    ContractResponseDTO createContract(ContractCreateDTO dto);
}