package com.example.bank.task;

import com.example.bank.entity.CreditCard;
import com.example.bank.repository.CreditCardRepository;
import com.example.bank.service.CreditCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BillingTask {

    private final CreditCardRepository creditCardRepository;
    private final CreditCardService creditCardService;

    /**
     * 每天凌晨 1:00 执行一次
     * 检查哪些信用卡在今天出账
     */
//    @Scheduled(cron = "0 0 1 * * ?")
    @Scheduled(cron = "0 * * * * ?")
    public void executeMonthlyBilling() {
        log.info("开始扫描今日需出账单的信用卡...");

        // 获取今天是几号
        int today = LocalDate.now().getDayOfMonth();

        // 1. 查找所有账单日(billingDate)是今天的信用卡
        // 注意：你需要在 CreditCardRepository 中写个简单的 findByBillingDate 方法
        List<CreditCard> cards = creditCardRepository.findByBillingDate(today);

        // 2. 遍历并生成账单
        for (CreditCard card : cards) {
            try {
                creditCardService.createBillForCard(card);
            } catch (Exception e) {
                log.error("卡号 {} 出账失败: {}", card.getCardNumber(), e.getMessage());
            }
        }
        log.info("今日出账任务执行完毕。");
    }
}