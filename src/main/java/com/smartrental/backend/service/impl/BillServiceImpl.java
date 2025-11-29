package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.BillCreateDTO;
import com.smartrental.backend.dto.response.BillResponseDTO;
import com.smartrental.backend.entity.Bill;
import com.smartrental.backend.entity.Contract;
import com.smartrental.backend.mapper.BillMapper; // Import Mapper
import com.smartrental.backend.model.json.ServiceFee;
import com.smartrental.backend.repository.BillRepository;
import com.smartrental.backend.repository.ContractRepository;
import com.smartrental.backend.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final ContractRepository contractRepository;
    private final BillMapper billMapper; // Inject Mapper

    @Override
    @Transactional
    public BillResponseDTO createBill(BillCreateDTO dto) {
        // 1. Lấy hợp đồng
        Contract contract = contractRepository.findById(dto.getContractId())
                .orElseThrow(() -> new RuntimeException("Hợp đồng không tồn tại"));

        // 2. Tìm chỉ số cũ
        Bill lastBill = billRepository.findTopByContractIdOrderByCreatedAtDesc(contract.getId())
                .orElse(null);

        int electricOld = (lastBill != null) ? lastBill.getElectricNew() : 0;
        int waterOld = (lastBill != null) ? lastBill.getWaterNew() : 0;

        if (dto.getElectricNew() < electricOld || dto.getWaterNew() < waterOld) {
            throw new RuntimeException("Chỉ số mới không được nhỏ hơn chỉ số cũ!");
        }

        // 3. Tính tiền
        BigDecimal electricCost = contract.getElectricPrice()
                .multiply(BigDecimal.valueOf(dto.getElectricNew() - electricOld));

        BigDecimal waterCost = contract.getWaterPrice()
                .multiply(BigDecimal.valueOf(dto.getWaterNew() - waterOld));

        BigDecimal servicesCost = BigDecimal.ZERO;
        if (contract.getServiceFees() != null) {
            for (ServiceFee fee : contract.getServiceFees()) {
                if (fee.getPrice() != null) {
                    servicesCost = servicesCost.add(fee.getPrice());
                }
            }
        }

        BigDecimal totalAmount = contract.getMonthlyRent()
                .add(electricCost)
                .add(waterCost)
                .add(servicesCost);

        // 4. Lưu Bill
        Bill bill = Bill.builder()
                .contract(contract)
                .month(dto.getMonth())
                .year(dto.getYear())
                .electricOld(electricOld)
                .waterOld(waterOld)
                .electricNew(dto.getElectricNew())
                .waterNew(dto.getWaterNew())
                .totalAmount(totalAmount)
                .status(Bill.Status.UNPAID)
                .build();

        Bill savedBill = billRepository.save(bill);

        // 5. Trả về DTO qua Mapper
        return billMapper.toResponse(savedBill);
    }
}