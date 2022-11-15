package com.giftmaseya.conveyorservice.controller;

import com.giftmaseya.conveyorservice.dto.CreditDTO;
import com.giftmaseya.conveyorservice.dto.LoanApplicationRequestDTO;
import com.giftmaseya.conveyorservice.dto.LoanOfferDTO;
import com.giftmaseya.conveyorservice.dto.ScoringDataDTO;
import com.giftmaseya.conveyorservice.service.CalculationService;
import com.giftmaseya.conveyorservice.service.OfferService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Api(value = "REST APIs for the conveyor-resources")
@RestController
@RequestMapping("/conveyor")
@AllArgsConstructor
public class ConveyorController {

    private final OfferService offerService;
    private final CalculationService calculationService;

    @ApiOperation(value = "Generate loan offers")
    @PostMapping("/offers")
    public ResponseEntity<List<LoanOfferDTO>> loanOffers(@Valid @RequestBody LoanApplicationRequestDTO application) {
        List<LoanOfferDTO> loanOfferDTOS = offerService.loanOffers(application);
        return ResponseEntity.ok(loanOfferDTOS);
    }

    @ApiOperation(value = "Perform relevant credit calculations")
    @PostMapping("/calculation")
    public ResponseEntity<CreditDTO> calculations(@RequestBody ScoringDataDTO scoringDataDTO) {
        CreditDTO creditDTO = calculationService.fillCreditInfo(scoringDataDTO);
        return ResponseEntity.ok(creditDTO);
    }


}
