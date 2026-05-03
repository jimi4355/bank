package com.example.bank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_records")
@Getter
@Setter
@NoArgsConstructor
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromAccountNumber; // 转出账号
    private String toAccountNumber;   // 转入账号
    private BigDecimal amount;        // 金额
    private LocalDateTime timestamp;  // 交易时间
    private String status;           // 状态：SUCCESS / FAILED

    // 方便快捷创建对象的构造函数
    public TransactionRecord(String from, String to, BigDecimal amount, String status) {
        this.fromAccountNumber = from;
        this.toAccountNumber = to;
        this.amount = amount;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}