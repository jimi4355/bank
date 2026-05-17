package com.example.bank.service;

import com.example.bank.entity.Account;
import com.example.bank.entity.CreditCard;
import com.example.bank.entity.TransactionType;
import com.example.bank.exception.BusinessException;
import com.example.bank.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;

    /**
     * 1. 信用卡消费 (增加负债)
     * 使用悲观锁防止并发导致超额消费
     */
    @Transactional(rollbackFor = Exception.class)
    public void consume(String cardNumber, BigDecimal amount) {
        // 校验金额
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(4001, "消费金额必须大于零");
        }

        // 使用 WithLock 查询，锁定该行直到事务结束
        CreditCard card = creditCardRepository.findByCardNumberWithLock(cardNumber)
                .orElseThrow(() -> new BusinessException(4003, "信用卡号不存在"));

        if (!"NORMAL".equals(card.getStatus())) {
            throw new BusinessException(4006, "卡片状态异常: " + card.getStatus());
        }

        // 核心逻辑：当前欠款 + 本次消费 <= 总额度
        BigDecimal newDebt = card.getCurrentDebt().add(amount);
        if (newDebt.compareTo(card.getCreditLimit()) > 0) {
            log.warn("消费失败：卡号 {} 额度不足", cardNumber);
            throw new BusinessException(4002, "信用额度不足");
        }

        card.setCurrentDebt(newDebt);
        creditCardRepository.save(card);

        // 记录流水并触发 WebSocket 推送
        transactionService.recordAndNotify(cardNumber, null, amount, TransactionType.WITHDRAW);
        log.info("信用卡消费成功：卡号 {}, 金额 {}", cardNumber, amount);
    }

    /**
     * 2. 信用卡还款 (从储蓄卡转入)
     * 涉及两个实体的状态变更，必须保证原子性
     */
    @Transactional(rollbackFor = Exception.class)
    public void repay(String cardNumber, String fromAccountNo, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(4001, "还款金额必须大于零");
        }

        // 锁定信用卡
        CreditCard card = creditCardRepository.findByCardNumberWithLock(cardNumber)
                .orElseThrow(() -> new BusinessException(4003, "信用卡不存在"));

        // 1. 调用 AccountService 扣减储蓄卡余额 (内部已包含余额校验和流水记录)
        // 这里的 amount 传负值表示支出
        accountService.updateBalance(fromAccountNo, amount.negate());

        // 2. 减少信用卡欠款
        BigDecimal remainingDebt = card.getCurrentDebt().subtract(amount);
        // 如果还多了，金融逻辑上会变成“溢缴款”，这里暂处理为最小 0
        card.setCurrentDebt(remainingDebt.max(BigDecimal.ZERO));
        creditCardRepository.save(card);

        // 3. 记录信用卡入账流水
        transactionService.recordAndNotify(fromAccountNo, cardNumber, amount, TransactionType.TRANSFER);
        log.info("信用卡还款成功：卡号 {}, 来源储蓄卡 {}", cardNumber, fromAccountNo);
    }

    /**
     * 3. 申请开卡 (初始化)
     */
    @Transactional
    public CreditCard applyCard(String cardNumber, BigDecimal initialLimit) {
        CreditCard card = new CreditCard();
        card.setCardNumber(cardNumber);
        card.setCreditLimit(initialLimit);
        card.setCurrentDebt(BigDecimal.ZERO);
        card.setBillingDate(5);  // 默认每月5号出账单
        card.setDueDate(25);     // 默认每月25号还款
        card.setStatus("NORMAL");
        return creditCardRepository.save(card);
    }

    /**
     * 4. 调整额度
     */
    @Transactional
    public void adjustLimit(String cardNumber, BigDecimal newLimit) {
        CreditCard card = creditCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new BusinessException(4003, "信用卡不存在"));
        card.setCreditLimit(newLimit);
        creditCardRepository.save(card);
    }
}