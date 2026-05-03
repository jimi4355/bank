package com.example.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;    // 状态码：200成功，400业务异常，500系统异常
    private String message;  // 提示信息
    private T data;          // 具体负载数据

    // 快捷返回成功的方法
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    // 快捷返回失败的方法
    public static <T> Result<T> error(String message) {
        return new Result<>(400, message, null);
    }
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}