package com.example.bank.service;

import ch.qos.logback.classic.Logger;
import com.example.bank.entity.Transaction;
import com.example.bank.entity.TransactionType;
import com.example.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
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

    /**
     * 发送个人通知提醒
     * @param identifier 识别号（可以是 cardNumber 或 userId，取决于你的前端订阅逻辑）
     * @param message 提醒内容
     */
    public void notifyUser(String identifier, String message) {
        log.info("准备发送通知给 [{}]: {}", identifier, message);

        // 构建一个简单的通知对象，方便前端解析
        Map<String, String> payload = new HashMap<>();
        payload.put("type", "NOTIFICATION");
        payload.put("content", message);
        payload.put("timestamp", LocalDateTime.now().toString());

        // 发送到指定频道，例如：/topic/notifications/6222001
        // 你的 test.html 需要监听这个路径
        messagingTemplate.convertAndSend("/topic/notifications/" + identifier, payload);
    }
}