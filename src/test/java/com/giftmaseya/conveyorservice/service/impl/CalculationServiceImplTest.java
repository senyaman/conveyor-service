package com.giftmaseya.conveyorservice.service.impl;

import com.giftmaseya.conveyorservice.dto.EmploymentDTO;
import com.giftmaseya.conveyorservice.dto.ScoringDataDTO;
import com.giftmaseya.conveyorservice.exception.ConveyorException;
import com.giftmaseya.conveyorservice.utils.EmploymentStatusEnum;
import com.giftmaseya.conveyorservice.utils.GenderEnum;
import com.giftmaseya.conveyorservice.utils.MaritalStatusEnum;
import com.giftmaseya.conveyorservice.utils.PositionEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.giftmaseya.conveyorservice.utils.EmploymentStatusEnum.BUSINESS_OWNER;
import static com.giftmaseya.conveyorservice.utils.PositionEnum.MIDDLE_MANAGER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class CalculationServiceImplTest {

    private CalculationServiceImpl calculationService;
    private ScoringDataDTO scoringDataDTO;
    private EmploymentDTO employmentDTO;

    @BeforeEach
    void setUp() {
        calculationService = new CalculationServiceImpl();
        employmentDTO = new EmploymentDTO(
                BUSINESS_OWNER,
                "458256",
                BigDecimal.valueOf(25000),
                MIDDLE_MANAGER,
                13,
                4
        );

        scoringDataDTO = new ScoringDataDTO(
                BigDecimal.valueOf(15000),
                6,
                "Gift",
                "Masenya",
                "Senyaman",
                GenderEnum.MALE,
                LocalDate.of(2000, 4, 15),
                "4444",
                "666666",
                LocalDate.of(1998, 10, 10),
                "HomeAffairs",
                MaritalStatusEnum.MARRIED,
                1,
                employmentDTO,
                "5698523641",
                true,
                false
        );

    }

    @Test
    void calcRate() {
        BigDecimal expectedRate = new BigDecimal("7.75");
        assertThat(calculationService.calcRate(scoringDataDTO)).isEqualTo(expectedRate);
    }

    @Test
    void calcRateIfSelfEmployed() {
        employmentDTO.setEmploymentStatus(EmploymentStatusEnum.SELF_EMPLOYED);
        BigDecimal expectedRate = new BigDecimal("5.75");
        assertThat(calculationService.calcRate(scoringDataDTO)).isEqualTo(expectedRate);
    }

    @Test
    void calcRateBasedOnPositionTopManager() {
        employmentDTO.setPosition(PositionEnum.TOP_MANAGER);
        BigDecimal expectedRate = new BigDecimal("5.75");
        assertThat(calculationService.calcRate(scoringDataDTO)).isEqualTo(expectedRate);
    }

    @Test
    void calcRateBasedOnFemaleAndAgeGreaterEqualThirtyFive() {
        scoringDataDTO.setGender(GenderEnum.FEMALE);
        scoringDataDTO.setBirthDate(LocalDate.of(1986, 1, 25));
        BigDecimal expectedRate = new BigDecimal("4.75");
        assertThat(calculationService.calcRate(scoringDataDTO)).isEqualTo(expectedRate);
    }

    @Test
    void calcRateBasedOnMaleAndAgeGreaterEqual30LessEqual55() {
        scoringDataDTO.setGender(GenderEnum.MALE);
        scoringDataDTO.setBirthDate(LocalDate.of(1986, 1, 25));
        BigDecimal expectedRate = new BigDecimal("4.75");
        assertThat(calculationService.calcRate(scoringDataDTO)).isEqualTo(expectedRate);
    }

    @Test
    void calcRateBasedOnMaritalStatusDivorced() {
        scoringDataDTO.setMaritalStatus(MaritalStatusEnum.DIVORCED);
        BigDecimal expectedRate = new BigDecimal("11.75");
        assertThat(calculationService.calcRate(scoringDataDTO)).isEqualTo(expectedRate);
    }

    @Test
    void calcRateIfDependentIsMoreThanOne() {
        scoringDataDTO.setDependentAmount(2);
        BigDecimal expectedRate = new BigDecimal("8.75");
        assertThat(calculationService.calcRate(scoringDataDTO)).isEqualTo(expectedRate);
    }

    @Test
    void throwExceptionIfUnemployed() {
        employmentDTO.setEmploymentStatus(EmploymentStatusEnum.UNEMPLOYED);
        assertThatThrownBy(() -> calculationService.calcRate(scoringDataDTO))
                .isInstanceOf(ConveyorException.class)
                .hasMessageContaining("Unemployed individual does not qualify for a loan");
    }

    @Test
    void throwExceptionIfAgeLessThanTwenty() {
        scoringDataDTO.setBirthDate(LocalDate.of(2004, 4, 5));
        assertThatThrownBy(() -> calculationService.calcRate(scoringDataDTO))
                .isInstanceOf(ConveyorException.class)
                .hasMessageContaining("rejection: persons under 20 do not qualify for a loan");
    }

    @Test
    void throwExceptionIfAgeMoreThanSixty() {
        scoringDataDTO.setBirthDate(LocalDate.of(1960, 4, 5));
        assertThatThrownBy(() -> calculationService.calcRate(scoringDataDTO))
                .isInstanceOf(ConveyorException.class)
                .hasMessageContaining("rejection: persons over 60 do not qualify for a loan");
    }

    @Test
    void throwExceptionIfLoanAmountTwentyTimesSalary() {
        scoringDataDTO.setAmount(BigDecimal.valueOf(500001));
        assertThatThrownBy(() -> calculationService.calcRate(scoringDataDTO))
                .isInstanceOf(ConveyorException.class)
                .hasMessageContaining("requested loan amount cannot be 20 times your salary");
    }

    @Test
    void throwExceptionIfWorkExperienceTotalLessTwelve() {
        employmentDTO.setWorkExperienceTotal(10);
        assertThatThrownBy(() -> calculationService.calcRate(scoringDataDTO))
                .isInstanceOf(ConveyorException.class)
                .hasMessageContaining("refusal: total work experience not enough, less than 12 months");
    }

    @Test
    void throwExceptionIfWorkExperienceCurrentLessThree() {
        employmentDTO.setWorkExperienceCurrent(2);
        assertThatThrownBy(() -> calculationService.calcRate(scoringDataDTO))
                .isInstanceOf(ConveyorException.class)
                .hasMessageContaining("refusal: current work experience not enough, less than 3 months");
    }

    @Test
    void calcRateBasedOnInsuranceAndSalaryClient() {
        BigDecimal expectedRate1 = new BigDecimal("11.25");
        assertThat(calculationService.calcRate(false, false)).isEqualTo(expectedRate1);

        BigDecimal expectedRate2 = new BigDecimal("10.25");
        assertThat(calculationService.calcRate(false, true)).isEqualTo(expectedRate2);

        BigDecimal expectedRate3 = new BigDecimal("10.25");
        assertThat(calculationService.calcRate(true, false)).isEqualTo(expectedRate3);

        BigDecimal expectedRate4 = new BigDecimal("8.25");
        assertThat(calculationService.calcRate(true, true)).isEqualTo(expectedRate4);
    }

    @Test
    void calculateAgeIfLessThanEighteen() {
        scoringDataDTO.setBirthDate(LocalDate.of(2006, 2, 15));
        assertThatThrownBy(() -> calculationService.calculateAge(scoringDataDTO))
                .isInstanceOf(ConveyorException.class)
                .hasMessageContaining("Age cannot be less than 18 years old");
    }

    @Test
    void calculateAgeIfBirthDateIsNull() {
        scoringDataDTO.setBirthDate(null);
        assertThatThrownBy(() -> calculationService.calculateAge(scoringDataDTO))
                .isInstanceOf(ConveyorException.class)
                .hasMessageContaining("value of birthDate cannot be null");
    }

    @Test
    void calculatePsk() {
        BigDecimal amount = scoringDataDTO.getAmount();
        BigDecimal rate = new BigDecimal("11.25");
        Integer term = scoringDataDTO.getTerm();
        BigDecimal expectedPsk = new BigDecimal("20695.27");

        assertThat(calculationService.calcPsk(amount, rate, term)).isEqualTo(expectedPsk);
    }

    @Test
    void calcMonthlyPayment() {
        BigDecimal amount = scoringDataDTO.getAmount();
        BigDecimal rate = new BigDecimal("11.25");
        Integer term = scoringDataDTO.getTerm();
        BigDecimal monthlyPayment = new BigDecimal("287.44");

        assertThat(calculationService.calcMonthlyPayment(amount, rate, term)).isEqualTo(monthlyPayment);
    }

}