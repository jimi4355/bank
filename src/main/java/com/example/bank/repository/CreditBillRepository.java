package com.example.bank.repository;

import com.example.bank.entity.CreditBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CreditBillRepository extends JpaRepository<CreditBill, Long> {

    boolean existsByCardNumberAndBillingCycle(String cardNumber, String billingCycle);

    List<CreditBill> findAllByDueDateLessThanEqualAndStatusNot(LocalDate date, CreditBill.BillStatus status);
}