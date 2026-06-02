package com.example.bank.controller;

import com.example.bank.dto.CreditCardApplyDTO;
import com.example.bank.dto.Result;
import com.example.bank.entity.CreditCard;
import com.example.bank.service.CreditCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService creditCardService;

    // 1. 信用卡消费
    @PostMapping("/consume")
    public Result<String> consume(@RequestParam String cardNumber, @RequestParam BigDecimal amount) {
        creditCardService.consume(cardNumber, amount);
        return Result.success("刷卡成功，负债增加");
    }

    // 2. 信用卡还款
    @PostMapping("/repay")
    public Result<String> repay(@RequestParam String cardNumber,
                                @RequestParam String fromAccountNo,
                                @RequestParam BigDecimal amount) {
        creditCardService.repay(cardNumber, fromAccountNo, amount);
        return Result.success("还款成功，信用额度已恢复");
    }

    // 3. 申请开卡
    @PostMapping("/apply")
    public Result<CreditCard> apply(@RequestBody CreditCardApplyDTO dto) {
        return Result.success(creditCardService.applyCard(
                dto.getUserId(),
                dto.getCardNumber(),
                dto.getLimit()
        ));
    }
}