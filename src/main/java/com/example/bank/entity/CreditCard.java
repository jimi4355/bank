package com.example.bank.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "credit_cards")
public class CreditCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String cardNumber;

    private BigDecimal creditLimit; // 授信总额
    private BigDecimal currentDebt; // 当前欠款

    private Integer billingDate;    // 账单日 (1-28)
    private Integer dueDate;        // 还款日 (1-28)

    private String status;          // NORMAL, FROZEN, CLOSED

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user; // 关联你的用户实体
}