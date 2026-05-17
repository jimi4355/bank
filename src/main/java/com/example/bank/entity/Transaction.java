package com.example.bank.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "transaction_records") // 整合后的表名，避开关键字
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromAccount; // 对应 fromAccountNumber
    private String toAccount;   // 对应 toAccountNumber

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;   // 交易类型：DEPOSIT, WITHDRAW, TRANSFER

    private String status;          // 交易状态：SUCCESS, FAILED

    private LocalDateTime timestamp;
    private String description;     // 备注

    // 整合后的全能构造函数
    public Transaction(String from, String to, BigDecimal amount, TransactionType type, String status) {
        this.fromAccount = from;
        this.toAccount = to;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}