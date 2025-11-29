package com.smartrental.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BillCreateDTO {
    @NotNull private Long contractId;
    @NotNull private Integer month;
    @NotNull private Integer year;

    @NotNull private Integer electricNew;
    @NotNull private Integer waterNew;
}