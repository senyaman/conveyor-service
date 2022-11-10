package com.giftmaseya.conveyorservice.service.impl;

import com.giftmaseya.conveyorservice.dto.CreditDTO;
import com.giftmaseya.conveyorservice.dto.EmploymentDTO;
import com.giftmaseya.conveyorservice.dto.PaymentScheduleElement;
import com.giftmaseya.conveyorservice.dto.ScoringDataDTO;
import com.giftmaseya.conveyorservice.service.CalculationService;
import com.giftmaseya.conveyorservice.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CalculationServiceImpl implements CalculationService {

    @Override
    public BigDecimal calcRate(ScoringDataDTO scoring) {

        log.info("*******scoring******* for {} {} {}", scoring.getFirstName(), scoring.getMiddleName(), scoring.getLastName());

        BigDecimal rate = new BigDecimal(String.valueOf(AppConstants.INITIAL_RATE));

        EmploymentDTO employmentInfo = scoring.getEmployment();

        if(employmentInfo.getEmploymentStatus() == EmploymentStatusEnum.SELF_EMPLOYED) {
            log.info("rate increases by 1 with employment status of SELF_EMPLOYED");
            rate = rate.add(BigDecimal.ONE);
        } else if (employmentInfo.getEmploymentStatus() == EmploymentStatusEnum.UNEMPLOYED) {
            log.info("Refusal: cannot offer loan to an unemployed individual");
        } else if (employmentInfo.getEmploymentStatus() == EmploymentStatusEnum.BUSINESS_OWNER) {
            log.info("rate increases by three for employment status of BUSINESS_OWNER");
            rate = rate.add(BigDecimal.valueOf(3));
        }

        if(employmentInfo.getPosition() == PositionEnum.TOP_MANAGER) {
            log.info("rate decrease by 4 for top manager");
            rate = rate.subtract(BigDecimal.valueOf(4));
        }

        if(employmentInfo.getPosition() == PositionEnum.MIDDLE_MANAGER) {
            log.info("rate decreases by 2 for middle manager");
            rate = rate.subtract(BigDecimal.valueOf(2));
        }

        if(scoring.getAmount().compareTo(employmentInfo.getSalary().multiply(BigDecimal.valueOf(20))) > 0) {
            log.info("requested loan amount cannot be 20 times your salary");
        }

        if(scoring.getMaritalStatus() == MaritalStatusEnum.MARRIED) {
            log.info("rate is reduced by 3 because marital status is MARRIED");
            rate = rate.subtract(BigDecimal.valueOf(3));
        } else if(scoring.getMaritalStatus() == MaritalStatusEnum.DIVORCED) {
            log.info("rate is increased by 1 because marital status is DIVORCED");
            rate = rate.add(BigDecimal.valueOf(1));
        }

        if(scoring.getDependentAmount() > 1) {
            log.info("rate increases by 1 since number of dependents is greater than 1");
            rate = rate.add(BigDecimal.ONE);
        }

        long age = calculateAge(scoring);
        if(age < 20) {
            log.info("rejection: persons under 20 do not qualify for a loan");
        } else if (age > 60) {
            log.info("rejection: persons over 60 do not qualify for a loan");
        }

        if((scoring.getGender() == GenderEnum.FEMALE) && (age >= 35 && age <=60)) {
            log.info("rate is reduced by 3 because gender is FEMALE and age is between 35 and 60 ");
            rate = rate.subtract(BigDecimal.valueOf(3));
        }else if((scoring.getGender() == GenderEnum.MALE) && (age >= 30 && age <= 55)) {
            log.info("rate is reduced by 3 because gender is MALE and age is between 30 and 55 ");
            rate = rate.subtract(BigDecimal.valueOf(3));
        }

        if(employmentInfo.getWorkExperienceTotal() < 12) {
            log.info("refusal: total work experience not enough, less than 12 months");
        } else if(employmentInfo.getWorkExperienceCurrent() < 3) {
            log.info("refusal: current work experience not enough, less than 3 months");
        }

        return rate;

    }

    @Override
    public BigDecimal calcRate(Boolean isInsuranceEnabled, Boolean isSalaryClient) {

        BigDecimal rate = AppConstants.INITIAL_RATE;

        if(!isInsuranceEnabled && !isSalaryClient) {
            rate = rate.add(BigDecimal.valueOf(1.5));
        }

        if(!isInsuranceEnabled && isSalaryClient) {
            rate = rate.add(BigDecimal.valueOf(0.5));
        }

        if(isInsuranceEnabled && !isSalaryClient) {
            rate = rate.add(BigDecimal.valueOf(0.5));
        }

        if(isInsuranceEnabled && isSalaryClient) {
            rate = rate.subtract(BigDecimal.valueOf(1.5));
        }

        return rate;
    }

    @Override
    public long calculateAge(ScoringDataDTO scoring) {
        if(scoring.getBirthDate() != null) {
            return Period.between(scoring.getBirthDate(), LocalDate.now()).getYears();
        } else {
            return 0;
        }
    }

    @Override
    public BigDecimal calculatePsk(ScoringDataDTO scoringDataDTO, Integer term) {

        BigDecimal monthlyPayment = calcMonthlyPayment(scoringDataDTO, term);
        int numberOfPayments = term * AppConstants.BASE_PERIOD;

        return monthlyPayment.multiply(BigDecimal.valueOf(numberOfPayments));
    }

    @Override
    public BigDecimal calculatePsk(BigDecimal monthlyPayment, Integer term) {
        term = term * AppConstants.BASE_PERIOD;
        return monthlyPayment.multiply(BigDecimal.valueOf(term));
    }

    @Override
    public BigDecimal calcMonthlyPayment(ScoringDataDTO scoringDataDTO, Integer term) {

        MathContext mc = new MathContext(2);

        BigDecimal principal = scoringDataDTO.getAmount();
        BigDecimal rate = calcRate(scoringDataDTO);
        int numberOfPayments = term * AppConstants.BASE_PERIOD;

        BigDecimal monthlyInterestRate = rate.divide(BigDecimal.valueOf(100), mc)
                .divide(BigDecimal.valueOf(AppConstants.BASE_PERIOD), mc);

        BigDecimal numerator = monthlyInterestRate.multiply((BigDecimal.ONE.add(monthlyInterestRate)).pow(numberOfPayments));

        BigDecimal denominator = (BigDecimal.ONE.add(monthlyInterestRate)).pow(numberOfPayments).subtract(BigDecimal.ONE);

        return principal.multiply(numerator.divide(denominator, mc));
    }

    @Override
    public BigDecimal calcMonthlyPayment(BigDecimal amount, BigDecimal rate, Integer term) {

        MathContext mc = new MathContext(2);

        int numberOfPayments = term * AppConstants.BASE_PERIOD;

        BigDecimal monthlyInterestRate = rate.divide(BigDecimal.valueOf(100), mc)
                .divide(BigDecimal.valueOf(AppConstants.BASE_PERIOD), mc);

        BigDecimal numerator = monthlyInterestRate.multiply((BigDecimal.ONE.add(monthlyInterestRate)).pow(numberOfPayments));

        BigDecimal denominator = (BigDecimal.ONE.add(monthlyInterestRate)).pow(numberOfPayments).subtract(BigDecimal.ONE);

        return amount.multiply(numerator.divide(denominator, mc));
    }

    @Override
    public CreditDTO fillCreditInfo(ScoringDataDTO scoring) {

        BigDecimal rate = calcRate(scoring);
        BigDecimal monthlyPayment = calcMonthlyPayment(scoring, scoring.getTerm());
        BigDecimal psk = calculatePsk(scoring, scoring.getTerm());

        CreditDTO creditDTO = new CreditDTO();
        creditDTO.setAmount(scoring.getAmount());
        creditDTO.setTerm(scoring.getTerm());
        creditDTO.setMonthlyPayment(monthlyPayment);
        creditDTO.setRate(rate);
        creditDTO.setPsk(psk);
        creditDTO.setIsInsuranceEnabled(scoring.getIsInsuranceEnabled());
        creditDTO.setIsSalaryClient(scoring.getIsSalaryClient());
        creditDTO.setPaymentSchedule(generatePaymentSchedule(scoring));

        return creditDTO;

    }

    @Override
    public List<PaymentScheduleElement> generatePaymentSchedule(ScoringDataDTO scoringDataDTO) {

        MathContext mc = new MathContext(2);

        BigDecimal rate = calcRate(scoringDataDTO).divide(BigDecimal.valueOf(100), mc);
        BigDecimal totalPayment = calcMonthlyPayment(scoringDataDTO, scoringDataDTO.getTerm());
        BigDecimal remainingDebt = calculatePsk(scoringDataDTO, scoringDataDTO.getTerm());
        BigDecimal monthlyInterestRate = rate.divide(BigDecimal.valueOf(AppConstants.BASE_PERIOD), mc);
        int numberOfPayments = scoringDataDTO.getTerm() * AppConstants.BASE_PERIOD;

        List<PaymentScheduleElement> schedule = new ArrayList<>();

        for(int i = 1; i <= numberOfPayments; i++) {
            BigDecimal interestPayment = remainingDebt.multiply(monthlyInterestRate);
            BigDecimal debtPayment = totalPayment.subtract(remainingDebt.multiply(monthlyInterestRate));
            remainingDebt = remainingDebt.subtract(debtPayment);

            if(remainingDebt.compareTo(BigDecimal.ZERO) < 0) {
                remainingDebt = BigDecimal.valueOf(0);
            }

            schedule.add(new PaymentScheduleElement(
                    i,
                    LocalDate.now().plusMonths(i),
                    totalPayment,
                    interestPayment,
                    debtPayment,
                    remainingDebt));
        }

        return schedule;
    }

}
