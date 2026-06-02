package com.example.bank.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditCardApplyDTO {
    private Long userId;
    private String cardNumber;
    private BigDecimal limit;
    private String defaultRepayAccount; // 顺便把还款账户也带上
}