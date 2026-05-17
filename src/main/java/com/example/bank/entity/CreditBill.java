package com.example.bank.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "credit_bills")
public class CreditBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cardNumber; // 关联的信用卡号

    private String billingCycle; // 账单周期，例如 "2026-05"

    private BigDecimal totalAmount;  // 账单总金额（该月消费汇总）
    private BigDecimal paidAmount;   // 已还金额
    private BigDecimal unpaidAmount; // 剩余未还金额

    private LocalDate dueDate;       // 到期还款日

    @Enumerated(EnumType.STRING)
    private BillStatus status;       // 账单状态

    private LocalDateTime createdAt; // 账单生成时间

    public enum BillStatus {
        UNPAID,     // 待还款
        PARTIAL,    // 部分还款
        PAID,       // 已结清
        OVERDUE     // 已逾期
    }

    // 快捷构造函数
    public CreditBill(String cardNumber, String billingCycle, BigDecimal totalAmount, LocalDate dueDate) {
        this.cardNumber = cardNumber;
        this.billingCycle = billingCycle;
        this.totalAmount = totalAmount;
        this.unpaidAmount = totalAmount;
        this.paidAmount = BigDecimal.ZERO;
        this.dueDate = dueDate;
        this.status = BillStatus.UNPAID;
        this.createdAt = LocalDateTime.now();
    }
}