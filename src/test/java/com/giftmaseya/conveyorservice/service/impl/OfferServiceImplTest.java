package com.giftmaseya.conveyorservice.service.impl;

import com.giftmaseya.conveyorservice.dto.LoanApplicationRequestDTO;
import com.giftmaseya.conveyorservice.dto.LoanOfferDTO;
import com.giftmaseya.conveyorservice.service.CalculationService;
import com.giftmaseya.conveyorservice.service.OfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class OfferServiceImplTest {

    @Mock
    private CalculationService calculationService;
    private OfferService offerService;
    private LoanApplicationRequestDTO loanApplication;

    @BeforeEach
    void setUp() {
        offerService = new OfferServiceImpl(calculationService);
        loanApplication = new LoanApplicationRequestDTO(
                BigDecimal.valueOf(15000),
                6,
                "Gift",
                "Masenya",
                "Senyaman",
                "senyaman@gmail.com",
                LocalDate.of(2000, 4, 4),
                "1234",
                "123456"
        );
    }

    @Test
    void generatingLoanOffers() {
        List<LoanOfferDTO> loanOffer = offerService.loanOffers(loanApplication);
        assertNotNull(loanOffer);
        assertEquals(4, loanOffer.size());
    }

}