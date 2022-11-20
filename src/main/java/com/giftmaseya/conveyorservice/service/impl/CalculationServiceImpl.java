package com.giftmaseya.conveyorservice.service.impl;

import com.giftmaseya.conveyorservice.dto.CreditDTO;
import com.giftmaseya.conveyorservice.dto.EmploymentDTO;
import com.giftmaseya.conveyorservice.dto.PaymentScheduleElement;
import com.giftmaseya.conveyorservice.dto.ScoringDataDTO;
import com.giftmaseya.conveyorservice.exception.ConveyorException;
import com.giftmaseya.conveyorservice.service.CalculationService;
import com.giftmaseya.conveyorservice.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import paqua.loan.amortization.api.LoanAmortizationCalculator;
import paqua.loan.amortization.api.impl.LoanAmortizationCalculatorFactory;
import paqua.loan.amortization.dto.Loan;
import paqua.loan.amortization.dto.LoanAmortization;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            rate = rate.add(AppConstants.SELF_EMPLOYED_RATE);
        } else if (employmentInfo.getEmploymentStatus() == EmploymentStatusEnum.UNEMPLOYED) {
            log.info("Refusal: cannot offer loan to an unemployed individual");
            throw new ConveyorException("Unemployed individual does not qualify for a loan");
        } else if (employmentInfo.getEmploymentStatus() == EmploymentStatusEnum.BUSINESS_OWNER) {
            log.info("rate increases by three for employment status of BUSINESS_OWNER");
            rate = rate.add(AppConstants.BUSINESS_OWNER_RATE);
        }

        if(employmentInfo.getPosition() == PositionEnum.TOP_MANAGER) {
            log.info("rate decrease by 4 for top manager");
            rate = rate.subtract(AppConstants.TOP_MAN_RATE);
        }

        if(employmentInfo.getPosition() == PositionEnum.MIDDLE_MANAGER) {
            log.info("rate decreases by 2 for middle manager");
            rate = rate.subtract(AppConstants.MIDDLE_MAN_RATE);
        }

        if(employmentInfo.getSalary().multiply(BigDecimal.valueOf(20)).compareTo(scoring.getAmount()) < 0) {
            log.info("requested loan amount cannot be 20 times your salary");
            throw new ConveyorException("requested loan amount cannot be 20 times your salary");
        }

        if(scoring.getMaritalStatus() == MaritalStatusEnum.MARRIED) {
            log.info("rate is reduced by 3 because marital status is MARRIED");
            rate = rate.subtract(AppConstants.MARRIED_RATE);
        } else if(scoring.getMaritalStatus() == MaritalStatusEnum.DIVORCED) {
            log.info("rate is increased by 1 because marital status is DIVORCED");
            rate = rate.add(AppConstants.DIVORCED_RATE);
        }

        if(scoring.getDependentAmount() > 1) {
            log.info("rate increases by 1 since number of dependents is greater than 1");
            rate = rate.add(BigDecimal.ONE);
        }

        long age = calculateAge(scoring);
        if(age < 20) {
            log.info("rejection: persons under 20 do not qualify for a loan");
            throw new ConveyorException("rejection: persons under 20 do not qualify for a loan");
        } else if (age > 60) {
            log.info("rejection: persons over 60 do not qualify for a loan");
            throw new ConveyorException("rejection: persons over 60 do not qualify for a loan");
        }

        if((scoring.getGender() == GenderEnum.FEMALE) && (age >= 35)) {
            log.info("rate is reduced by 3 because gender is FEMALE and age is between 35 and 60 ");
            rate = rate.subtract(BigDecimal.valueOf(3));
        }else if((scoring.getGender() == GenderEnum.MALE) && (age >= 30 && age <= 55)) {
            log.info("rate is reduced by 3 because gender is MALE and age is between 30 and 55 ");
            rate = rate.subtract(BigDecimal.valueOf(3));
        }

        if(employmentInfo.getWorkExperienceTotal() < 12) {
            log.info("refusal: total work experience not enough, less than 12 months");
            throw new ConveyorException("refusal: total work experience not enough, less than 12 months");
        } else if(employmentInfo.getWorkExperienceCurrent() < 3) {
            log.info("refusal: current work experience not enough, less than 3 months");
            throw new ConveyorException("refusal: current work experience not enough, less than 3 months");
        }

        return rate;

    }

    @Override
    public BigDecimal calcRate(Boolean isInsuranceEnabled, Boolean isSalaryClient) {

        log.info("Calculating Rate");

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

        log.info("Checking for valid age");

        if(scoring.getBirthDate() != null) {
            int age = Period.between(scoring.getBirthDate(), LocalDate.now()).getYears();
            if(age >= 18) {
                return age;
            } else {
                throw new ConveyorException("Age cannot be less than 18 years old");
            }
        } else {
            throw new ConveyorException("value of birthDate cannot be null");
        }
    }

    @Override
    public BigDecimal calcMonthlyPayment(BigDecimal amount, BigDecimal rate, Integer term) {

        log.info("Calculating monthly installments of the loan");

        term = term * AppConstants.BASE_PERIOD;

        Loan loan = Loan.builder()
                .amount(amount)
                .rate(rate)
                .term(term)
                .build();

        LoanAmortizationCalculator calculator = LoanAmortizationCalculatorFactory.create();
        LoanAmortization amortization = calculator.calculate(loan);
        return amortization.getMonthlyPaymentAmount();
    }

    @Override
    public BigDecimal calcPsk(BigDecimal amount, BigDecimal rate, Integer term) {

        log.info("Calculating monthly installments of the loan");

        term = term * AppConstants.BASE_PERIOD;

        Loan loan = Loan.builder()
                .amount(amount)
                .rate(rate)
                .term(term)
                .build();

        LoanAmortizationCalculator calculator = LoanAmortizationCalculatorFactory.create();
        LoanAmortization amortization = calculator.calculate(loan);
        return amortization.getOverPaymentAmount().add(amount);
    }



    @Override
    public CreditDTO fillCreditInfo(ScoringDataDTO scoring) {

        log.info("Generating credit information");

        BigDecimal rate = calcRate(scoring);
        BigDecimal monthlyPayment = calcMonthlyPayment(scoring.getAmount(), rate, scoring.getTerm());
        BigDecimal psk = calcPsk(scoring.getAmount(), rate, scoring.getTerm());

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

        log.info("Generating the payment schedule");


        BigDecimal rate = calcRate(scoringDataDTO).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);
        BigDecimal totalPayment = calcMonthlyPayment(scoringDataDTO.getAmount(), rate, scoringDataDTO.getTerm());
        BigDecimal remainingDebt = calcPsk(scoringDataDTO.getAmount(), rate, scoringDataDTO.getTerm());
        BigDecimal monthlyInterestRate = rate.divide(BigDecimal.valueOf(AppConstants.BASE_PERIOD), 2, RoundingMode.HALF_EVEN);
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
