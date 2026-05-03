package com.example.bank.service;

import com.example.bank.entity.Account;
import com.example.bank.entity.TransactionRecord;
import com.example.bank.exception.BusinessException;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 银行核心业务层
 * 职责：处理转账逻辑、金额校验、事务管理
 */
@Service
@RequiredArgsConstructor // 自动生成构造器，注入 AccountRepository
public class BankService {

    private final AccountRepository accountRepository;
    private final TransactionRecordRepository recordRepository; // 注入新的仓库

    /**
     * 查询账户详情
     * @param accountNumber 账号
     * @return 账户实体
     */
    @Transactional(readOnly = true) // 开启只读事务，优化数据库性能
    public Account findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("该账号 [" + accountNumber + "] 不存在"));
    }

    /**
     * 核心转账业务
     * @param fromNo 支出方账号
     * @param toNo   存入方账号
     * @param amount 转账金额
     */
    @Transactional // 开启读写事务，保证原子性（要么都成功，要么都失败）
    public void transfer(String fromNo, String toNo, BigDecimal amount) {
        // 1. 严谨性检查：金额必须大于零
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("转账金额必须大于零");
        }

        // 2. 获取两个账户的最新状态（进入 Hibernate 持久化上下文）
        Account fromAccount = findAccount(fromNo);
        Account toAccount = findAccount(toNo);

        // 3. 校验余额
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(4002, "账户余额不足，当前余额: " + fromAccount.getBalance());
        }

        // 4. 执行内存中的金额变动
        // 这里的修改会被 Hibernate 自动检测到，并在事务提交时自动同步到数据库
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        // 5. 显式保存
        // 在 @Transactional 环境下，不写这两行也会自动更新
        // 写出来有助于理解数据流向
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 在方法最后添加：保存流水记录
        TransactionRecord record = new TransactionRecord(fromNo, toNo, amount, "SUCCESS");
        recordRepository.save(record);
    }
}