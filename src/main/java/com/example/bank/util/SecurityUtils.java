package com.example.bank.util;

public class SecurityUtils {
    /**
     * 脱敏身份证号：只保留前3位和后4位
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) return "****";
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }
}