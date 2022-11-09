package com.giftmaseya.conveyorservice.dto;

import com.giftmaseya.conveyorservice.utils.EmploymentStatusEnum;
import com.giftmaseya.conveyorservice.utils.PositionEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmploymentDTO {

    private EmploymentStatusEnum employmentStatus;
    private String employerINN;
    private BigDecimal salary;
    private PositionEnum position;
    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;
}
