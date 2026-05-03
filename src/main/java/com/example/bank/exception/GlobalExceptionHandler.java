package com.example.bank.exception;

import com.example.bank.dto.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常拦截器
 * 职责：捕获整个项目中抛出的异常，并统一转化为 Result 格式返回给前端
 */
@Slf4j // 方便打印日志
@RestControllerAdvice // 核心注解：结合了 @ControllerAdvice 和 @ResponseBody
public class GlobalExceptionHandler {


    /**
     * 处理程序员手动抛出的业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务逻辑异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理系统未知的崩溃（兜底逻辑）
     * 捕获所有不可预知的异常 (Exception)
     * 比如数据库连接断开了、空指针等
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统崩溃: ", e);
        return Result.error("服务器开小差了，请稍后再试");
    }
}