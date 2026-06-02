package com.example.bank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String phoneNumber;

    // --- 实名认证核心字段 ---

    private String realName; // 真实姓名

    @Column(unique = true)
    private String idCardNumber; // 身份证号（需唯一）

    @Enumerated(EnumType.STRING)
    private AuthStatus authStatus = AuthStatus.UNVERIFIED; // 认证状态

    private LocalDateTime authTime; // 认证通过时间

    // --- 账户关联 ---

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Account> accounts;

    /**
     * 定义认证状态枚举
     */
    public enum AuthStatus {
        UNVERIFIED, // 未认证
        PENDING,    // 审核中（上传了证件，等待系统核验）
        VERIFIED,   // 已认证（可以执行开卡、大额转账）
        REJECTED    // 已拒绝（信息不符或证件过期）
    }
}