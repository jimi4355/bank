package com.example.bank.task;

import com.example.bank.entity.CreditBill;
import com.example.bank.repository.CreditBillRepository;
import com.example.bank.service.CreditCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OverdueTask {

    private final CreditBillRepository billRepository;
    private final CreditCardService cardService;

    // 每天凌晨 2 点执行 (在出账单任务之后)
    @Scheduled(cron = "0 0 2 * * ?")
    public void processOverduePayments() {
        // 找出所有今天到期的未还账单
        List<CreditBill> overdueBills = billRepository.findAllByDueDateLessThanEqualAndStatusNot(
                LocalDate.now(), CreditBill.BillStatus.PAID);

        for (CreditBill bill : overdueBills) {
            cardService.handleOverdueBill(bill);
        }
    }
}