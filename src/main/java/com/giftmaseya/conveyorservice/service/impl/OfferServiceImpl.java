package com.giftmaseya.conveyorservice.service.impl;

import com.giftmaseya.conveyorservice.dto.LoanApplicationRequestDTO;
import com.giftmaseya.conveyorservice.dto.LoanOfferDTO;
import com.giftmaseya.conveyorservice.service.CalculationService;
import com.giftmaseya.conveyorservice.service.OfferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class OfferServiceImpl implements OfferService {

    private final CalculationService calculationService;

    public OfferServiceImpl(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Override
    public List<LoanOfferDTO> loanOffers(LoanApplicationRequestDTO loanApplication) {

        return List.of(
                createSingleOffer(false, false, loanApplication),
                createSingleOffer(false, true, loanApplication),
                createSingleOffer(true, false, loanApplication),
                createSingleOffer(true, true, loanApplication)
        );

    }

    @Override
    public LoanOfferDTO createSingleOffer(Boolean isInsuranceEnabled,
                                          Boolean isSalaryClient,
                                          LoanApplicationRequestDTO request) {

        log.info("Generating a single loan offer");
        BigDecimal rate = calculationService.calcRate(isInsuranceEnabled, isSalaryClient);
        BigDecimal requestedAmount = request.getAmount();
        Integer term = request.getTerm();
        BigDecimal monthlyPayment = calculationService.calcMonthlyPayment(requestedAmount, rate, term);
        BigDecimal totalAmount = calculationService.calculatePsk(monthlyPayment, term);

        LoanOfferDTO loanOffer = new LoanOfferDTO();

        loanOffer.setRequestedAmount(requestedAmount);
        loanOffer.setTotalAmount(totalAmount);
        loanOffer.setTerm(term);
        loanOffer.setMonthlyPayment(monthlyPayment);
        loanOffer.setRate(rate);
        loanOffer.setIsInsuranceEnabled(isInsuranceEnabled);
        loanOffer.setIsSalaryClient(isSalaryClient);

        return loanOffer;

    }
}
