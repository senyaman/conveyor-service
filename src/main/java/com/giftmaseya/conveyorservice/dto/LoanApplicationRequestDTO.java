package com.giftmaseya.conveyorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LoanApplicationRequestDTO {

    @DecimalMin(value = "10000", message = "Amount cannot be less than 10 000")
    private BigDecimal amount;

    @Min(value = 6, message = "Loan term cannot be less than 6")
    private Integer term;

    @Size(min = 2, max = 30, message = "firstName must be between 2 and 30 characters")
    private String firstName;

    @Size(min = 2, max = 30, message = "lastName must be between 2 and 30 characters")
    private String lastName;

    @Size(min = 2, max = 30, message = "middleName must be between 2 and 30 characters")
    private String middleName;

    @Email(regexp = "[\\w\\.]{2,50}@[\\w\\.]{2,20}", message = "Email address is invalid")
    private String email;

    private LocalDate birthDate;

    @Size(min = 4, max = 4, message = "passport series must be 4 characters long")
    private String passportSeries;

    @Size(min = 6, max = 6, message = "passport number must be six digits long")
    private String passportNumber;
}
