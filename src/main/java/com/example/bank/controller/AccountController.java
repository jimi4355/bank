package com.example.bank.controller;

import com.example.bank.dto.Result;
import com.example.bank.entity.Account;
import com.example.bank.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 账户管理控制层
 * 职责：负责接收 HTTP 请求，验证参数，并调用 Service 层处理业务
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor // 使用 Lombok 自动生成构造器，实现完美的依赖注入
public class AccountController {

    // 使用 final 修饰，由构造器注入，保证了 BankService 的不可变性和安全性
    private final BankService bankService;

    /**
     * 根据账号查询详情
     * GET /api/accounts/9527001
     */
    @GetMapping("/{accountNumber}")
    public Result<Account> getAccount(@PathVariable String accountNumber) {
        Account account = bankService.findAccount(accountNumber);
        // 这里只管写成功的逻辑，失败的情况会自动被 GlobalExceptionHandler 拦截
        return Result.success(account); // 以后 Postman 看到的将是一个带 code 和 message 的整洁 JSON
    }

    /**
     * 转账接口
     * POST /api/accounts/transfer?from=9527001&to=9527002&amount=100
     * 现在支持 Postman 的 JSON Body 传参了
     */
    @PostMapping("/transfer")
    public Result<String> transfer(@RequestBody java.util.Map<String, Object> params) {
        // 1. 从 JSON 中提取参数
        String from = (String) params.get("from");
        String to = (String) params.get("to");

        // 2. 将金额转为 BigDecimal（处理 JSON 数字类型）
        BigDecimal amount = new BigDecimal(params.get("amount").toString());

        // 3. 调用 Service
        bankService.transfer(from, to, amount);

        return Result.success("转账成功！");
    }
}