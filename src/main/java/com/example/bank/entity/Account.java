package com.example.bank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * 账户实体类
 * 对应数据库中的 accounts 表
 */
@Entity
@Table(name = "accounts")
@Getter // Lombok：自动生成 Getter
@Setter // Lombok：自动生成 Setter
@NoArgsConstructor // Lombok：生成无参构造函数（JPA 必须）
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 账号，设为唯一索引，方便快速查询
    @Column(unique = true, nullable = false)
    private String accountNumber;

    // 余额，使用 BigDecimal 保证精度
    @Column(nullable = false)
    private BigDecimal balance;

    // 在 Account 类中增加关联
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 乐观锁版本号
     * 作用：每次 update 时，JPA 会自动对比版本号并自增。
     * 如果两个事务同时修改此行，后提交的事务会因为版本号不匹配而失败，
     * 从而避免并发转账导致的数据错误。
     */
    @Version
    private Long version;

    /**
     * 全参构造函数，方便初始化数据
     */
    public Account(String accountNumber, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
}