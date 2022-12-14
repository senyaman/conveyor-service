package com.giftmaseya.conveyorservice.service;

import com.giftmaseya.conveyorservice.dto.CreditDTO;
import com.giftmaseya.conveyorservice.dto.PaymentScheduleElement;
import com.giftmaseya.conveyorservice.dto.ScoringDataDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CalculationService {
    BigDecimal calcRate(ScoringDataDTO scoring);
    BigDecimal calcRate(Boolean isInsuranceEnabled, Boolean isSalaryClient);
    long calculateAge(ScoringDataDTO scoring);
    BigDecimal calcMonthlyPayment(BigDecimal amount, BigDecimal rate, Integer term);
    BigDecimal calcPsk(BigDecimal amount, BigDecimal rate, Integer term);
    CreditDTO fillCreditInfo(ScoringDataDTO scoring);
    List<PaymentScheduleElement> generatePaymentSchedule(ScoringDataDTO scoringDataDTO);

}
