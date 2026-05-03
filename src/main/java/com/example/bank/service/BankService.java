package com.example.bank.service;

import com.example.bank.entity.Account;
import com.example.bank.entity.TransactionRecord;
import com.example.bank.exception.BusinessException;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 银行核心业务逻辑层
 * 职责：处理账户查询、转账校验、资金结算及实时通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankService {

    private final AccountRepository accountRepository;
    private final TransactionRecordRepository recordRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 根据账号查询账户详情
     */
    public Account findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(4003, "账号 " + accountNumber + " 不存在"));
    }

    /**
     * 核心转账业务
     * 使用 @Transactional 确保原子性：扣款、增资、流水记录、推送要么全部成功，要么全部回滚
     */
    @Transactional(rollbackFor = Exception.class)
    public void transfer(String fromNo, String toNo, BigDecimal amount) {
        // 1. 转账金额合法性检查
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(4001, "转账金额必须大于零");
        }

        // 2. 锁定并获取账户信息（防止并发操作下的金额错乱）
        Account fromAccount = findAccount(fromNo);
        Account toAccount = findAccount(toNo);

        // 不能给自己转账
        if (fromNo.equals(toNo)) {
            throw new BusinessException(4005, "不能向原账户发起转账");
        }

        // 3. 余额充足性校验
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            log.warn("转账失败：账户 {} 余额不足，当前余额：{}", fromNo, fromAccount.getBalance());
            throw new BusinessException(4002, "账户余额不足，当前余额: " + fromAccount.getBalance());
        }

        // 4. 执行账务变动
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        // 持久化更新
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 5. 生成交易流水记录
        // 严格匹配 TransactionRecord 字段名
        TransactionRecord record = new TransactionRecord();
        record.setFromAccountNumber(fromNo);     // 确保与实体类字段一致
        record.setToAccountNumber(toNo);       // 确保与实体类字段一致
        record.setAmount(amount);
        record.setStatus("SUCCESS");
        record.setTimestamp(LocalDateTime.now()); // 使用你定义的 timestamp 字段

        recordRepository.save(record);

        // WebSocket 推送
        Map<String, Object> notice = Map.of(
                "type", "TRANSFER_SUCCESS",
                "from", fromNo,
                "to", toNo,
                "amount", amount
        );
        messagingTemplate.convertAndSend("/topic/transfer", notice);

        log.info("转账成功：{} -> {}, 金额：{}", fromNo, toNo, amount);
    }
}