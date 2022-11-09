package com.giftmaseya.conveyorservice.controller;

import com.giftmaseya.conveyorservice.dto.CreditDTO;
import com.giftmaseya.conveyorservice.dto.LoanApplicationRequestDTO;
import com.giftmaseya.conveyorservice.dto.LoanOfferDTO;
import com.giftmaseya.conveyorservice.dto.ScoringDataDTO;
import com.giftmaseya.conveyorservice.service.CalculationService;
import com.giftmaseya.conveyorservice.service.OfferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/conveyor")
public class ConveyorController {

    private final OfferService offerService;
    private final CalculationService calculationService;

    public ConveyorController(OfferService offerService, CalculationService calculationService) {
        this.offerService = offerService;
        this.calculationService = calculationService;
    }

    @PostMapping("/offers")
    public ResponseEntity<List<LoanOfferDTO>> loanOffers(@Valid @RequestBody LoanApplicationRequestDTO application) {
        List<LoanOfferDTO> loanOfferDTOS = offerService.loanOffers(application);
        return ResponseEntity.ok(loanOfferDTOS);
    }

    @PostMapping("/calculation")
    public ResponseEntity<CreditDTO> calculations(@RequestBody ScoringDataDTO scoringDataDTO) {
        CreditDTO creditDTO = calculationService.fillCreditInfo(scoringDataDTO);
        return ResponseEntity.ok(creditDTO);
    }


}
