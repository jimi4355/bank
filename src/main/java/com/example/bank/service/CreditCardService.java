package com.example.bank.service;


import com.example.bank.entity.*;
import com.example.bank.exception.BusinessException;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.CreditBillRepository;
import com.example.bank.repository.CreditCardRepository;
import com.example.bank.repository.TransactionRepository;
import com.example.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    // 注入刚创建的 repository
    private final CreditBillRepository creditBillRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

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
     * 增加 userId 参数，并校验实名状态
     */
    @Transactional
    public CreditCard applyCard(Long userId, String cardNumber, BigDecimal initialLimit) {
        // 1. 查找用户并校验实名状态
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(4004, "用户不存在"));

        if (user.getAuthStatus() != User.AuthStatus.VERIFIED) {
            throw new BusinessException(4008, "请先完成实名认证再尝试开卡");
        }

        // 2. 初始化卡片信息
        CreditCard card = new CreditCard();
        card.setCardNumber(cardNumber);
        card.setCreditLimit(initialLimit);
        card.setCurrentDebt(BigDecimal.ZERO);
        card.setBillingDate(5);
        card.setDueDate(25);
        card.setStatus("NORMAL");

        // 3. 关联用户 (假设你的 CreditCard 实体中有 userId 字段)
        card.setUserId(userId);

        log.info("用户 {} (实名: {}) 成功申领信用卡: {}", userId, user.getRealName(), cardNumber);
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



    @Transactional(rollbackFor = Exception.class)
    public void createBillForCard(CreditCard card) {
        String cycle = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 1. 新增校验：检查是否已存在该卡本月的账单
        // 你需要在 CreditBillRepository 中定义 findByCardNumberAndBillingCycle
        boolean exists = creditBillRepository.existsByCardNumberAndBillingCycle(card.getCardNumber(), cycle);

        if (exists) {
            log.info("卡号 {} 在 {} 账期已出账，跳过生成。", card.getCardNumber(), cycle);
            return; // 直接返回，不再重复生成
        }

        // 2. 获取统计范围：从上个月今天到此时此刻
        LocalDateTime start = LocalDateTime.now().minusMonths(1);
        LocalDateTime end = LocalDateTime.now();

        // 3. 计算本期消费总额
        BigDecimal total = transactionRepository.sumMonthConsumption(card.getCardNumber(), start, end);
        if (total == null) total = BigDecimal.ZERO;

        // 4. 创建并保存账单 (还款日设为出账后20天)
        CreditBill bill = new CreditBill(
                card.getCardNumber(),
                cycle,
                total,
                LocalDate.now().plusDays(20)
        );

        creditBillRepository.save(bill);
        log.info("用户 {} 的账单已生成，金额: {}", card.getCardNumber(), total);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleOverdueBill(CreditBill bill) {
        // 使用 WithLock 锁定信用卡，防止并发冲突
        CreditCard card = creditCardRepository.findByCardNumberWithLock(bill.getCardNumber())
                .orElseThrow(() -> new BusinessException(4003, "未找到关联的信用卡"));

        String repayAccountNo = card.getDefaultRepayAccount();
        if (repayAccountNo == null || repayAccountNo.isEmpty()) {
            markAsOverdue(bill);
            return;
        }

        try {
            // 先检查余额，实现“能还多少还多少”
            // 修改 handleOverdueBill 里的这一行
            Account account = accountRepository.findByAccountNumber(repayAccountNo)
                    .orElseThrow(() -> new BusinessException(4003, "还款储蓄账户不存在"));
            BigDecimal balance = account.getBalance();
            BigDecimal unpaid = bill.getUnpaidAmount();

            // 实际扣款金额：取余额和欠款的最小值
            BigDecimal actualDeduct = balance.compareTo(unpaid) >= 0 ? unpaid : balance;

            if (actualDeduct.compareTo(BigDecimal.ZERO) > 0) {
                accountService.updateBalance(repayAccountNo, actualDeduct.negate());

                bill.setPaidAmount(bill.getPaidAmount().add(actualDeduct));
                bill.setUnpaidAmount(bill.getUnpaidAmount().subtract(actualDeduct));
                card.setCurrentDebt(card.getCurrentDebt().subtract(actualDeduct));

                log.info("账单 {} 部分/全部自动扣款成功: {}", bill.getId(), actualDeduct);
            }

            // 检查是否彻底还清
            if (bill.getUnpaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
                bill.setStatus(CreditBill.BillStatus.PAID);
            } else {
                markAsOverdue(bill); // 没还清依然算逾期
            }

        } catch (Exception e) {
            log.error("自动扣款过程发生异常: {}", e.getMessage());
            markAsOverdue(bill);
        }

        creditBillRepository.save(bill);
        creditCardRepository.save(card);
    }

    private void markAsOverdue(CreditBill bill) {
        bill.setStatus(CreditBill.BillStatus.OVERDUE);
        // 触发动账提醒（WebSocket）
        transactionService.notifyUser(bill.getCardNumber(), "您的账单已逾期，请及时处理以免影响征信。");
    }

}