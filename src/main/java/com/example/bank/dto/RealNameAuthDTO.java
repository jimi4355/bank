package com.example.bank.dto;

import lombok.Data;

@Data
public class RealNameAuthDTO {
    private String realName;
    private String idCardNumber;
    private String frontImageUrl; // 假设你已经上传了图片
}