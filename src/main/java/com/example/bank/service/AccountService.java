package com.example.bank.service;

import com.example.bank.entity.Account;
import com.example.bank.entity.TransactionType;
import com.example.bank.exception.BusinessException;
import com.example.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService; // 注入流水服务

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(4003, "账户不存在"));
    }

    /**
     * 统一余额更新逻辑
     * @param amount 变动金额（正数为存，负数为取）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBalance(String accountNumber, BigDecimal amount) {
        Account account = getAccount(accountNumber);
        BigDecimal newBalance = account.getBalance().add(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(4002, "余额不足，当前余额: " + account.getBalance());
        }

        account.setBalance(newBalance);
        accountRepository.save(account);

        // 根据金额正负判断交易类型，并记录流水
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            // 存款：from 为空，to 为自己
            transactionService.recordAndNotify(null, accountNumber, amount, TransactionType.DEPOSIT);
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            // 取款：from 为自己，to 为空
            transactionService.recordAndNotify(accountNumber, null, amount.abs(), TransactionType.WITHDRAW);
        }
    }

    public void deleteAccount(String accountNumber) {
        Account account = getAccount(accountNumber);
        accountRepository.delete(account);
    }
}