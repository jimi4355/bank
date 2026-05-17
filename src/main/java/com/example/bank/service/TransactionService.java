package com.example.bank.service;

import com.example.bank.entity.Transaction;
import com.example.bank.entity.TransactionType;
import com.example.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 记录流水并实时通知
     */
    @Transactional
    public void recordAndNotify(String from, String to, BigDecimal amount, TransactionType type) {
        // 1. 使用整合后的构造函数快速创建对象
        Transaction transaction = new Transaction(from, to, amount, type, "SUCCESS");

        // 2. 持久化到数据库
        transactionRepository.save(transaction);

        // 3. 动账提醒：推送到 WebSocket
        messagingTemplate.convertAndSend("/topic/transfer", transaction);
    }

    /**
     * 获取历史账单
     */
    public List<Transaction> getHistory(String accountNumber) {
        return transactionRepository.findByFromAccountOrToAccountOrderByTimestampDesc(accountNumber, accountNumber);
    }
}