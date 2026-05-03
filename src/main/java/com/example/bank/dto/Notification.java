package com.example.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Notification {
    private String type;    // TRANSFER_SUCCESS, BALANCE_ALARM
    private String content; // 具体的文字内容
    private long timestamp;
}