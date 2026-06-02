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

    @Column(unique = true, nullable = false, length = 20)
    private String cardNumber;

    // 建议设置默认值，防止空指针异常
    @Column(nullable = false)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal currentDebt = BigDecimal.ZERO;

    private Integer billingDate;    // 账单日
    private Integer dueDate;        // 还款日

    // 建议在数据库层面也增加默认值
    @Column(columnDefinition = "varchar(20) default 'NORMAL'")
    private String status = "NORMAL";

    /**
     * 关联用户
     * 改为 Long userId 还是 User 实体？
     * 在你的 Service 中使用了 userRepository.findById(userId)，
     * 建议这里直接存 Long userId 以简化查询，或者保留 User 对象用于 JPA 自动关联。
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 默认还款账户（储蓄卡号）
     */
    private String defaultRepayAccount;

    // --- 招行化进阶字段 ---

    /**
     * 临时额度
     */
    private BigDecimal tempLimit = BigDecimal.ZERO;

    /**
     * 临时额度到期时间
     */
    private java.time.LocalDateTime tempLimitExpiry;

    /**
     * 支付密码（加密存储，不同于登录密码）
     */
    private String paymentPassword;
}