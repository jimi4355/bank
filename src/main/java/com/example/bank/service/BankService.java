package com.example.bank.service;

import com.example.bank.entity.Account;
import com.example.bank.entity.TransactionType;
import com.example.bank.exception.BusinessException;
import com.example.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 银行核心业务逻辑层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankService {

    private final AccountRepository accountRepository;
    // 1. 注入新的 TransactionService，替换掉旧的 recordRepository
    private final TransactionService transactionService;


    public Account findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(4003, "账号 " + accountNumber + " 不存在"));
    }

    @Transactional(rollbackFor = Exception.class)
    public void transfer(String fromNo, String toNo, BigDecimal amount) {
        // 1. 合法性检查
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(4001, "转账金额必须大于零");
        }
        if (fromNo.equals(toNo)) {
            throw new BusinessException(4005, "不能向原账户发起转账");
        }

        // 2. 获取账户
        Account fromAccount = findAccount(fromNo);
        Account toAccount = findAccount(toNo);

        // 3. 余额校验
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            log.warn("转账失败：账户 {} 余额不足", fromNo);
            throw new BusinessException(4002, "账户余额不足");
        }

        // 4. 账务变动
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 5. 调用流水记录与实时推送
        // 直接使用整合后的服务，代码极度简化，且保证了事务一致性
        transactionService.recordAndNotify(fromNo, toNo, amount, TransactionType.TRANSFER);

        log.info("转账成功：{} -> {}, 金额：{}", fromNo, toNo, amount);
    }
}