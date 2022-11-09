package com.giftmaseya.conveyorservice.service;

import com.giftmaseya.conveyorservice.dto.LoanApplicationRequestDTO;
import com.giftmaseya.conveyorservice.dto.LoanOfferDTO;

import java.util.List;

public interface OfferService {
    List<LoanOfferDTO> loanOffers(LoanApplicationRequestDTO loanApplicationRequestDTO);
    LoanOfferDTO createSingleOffer(Boolean isInsuranceEnabled,
                                   Boolean isSalaryClient,
                                   LoanApplicationRequestDTO request);
}
