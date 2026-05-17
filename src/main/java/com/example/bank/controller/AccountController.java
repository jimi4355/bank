package com.example.bank.controller;

import com.example.bank.dto.Result;
import com.example.bank.entity.Account;
import com.example.bank.entity.Transaction;
import com.example.bank.service.AccountService;
import com.example.bank.service.BankService;
import com.example.bank.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final BankService bankService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping("/{accountNumber}")
    public Result<Account> getAccount(@PathVariable String accountNumber) {
        return Result.success(bankService.findAccount(accountNumber));
    }

    @PostMapping
    public Result<Account> createAccount(@RequestBody Account account) {
        return Result.success(accountService.createAccount(account));
    }

    @PutMapping("/deposit")
    public Result<String> deposit(@RequestParam String accountNumber, @RequestParam BigDecimal amount) {
        accountService.updateBalance(accountNumber, amount);
        return Result.success("存款成功");
    }

    @PutMapping("/withdraw")
    public Result<String> withdraw(@RequestParam String accountNumber, @RequestParam BigDecimal amount) {
        accountService.updateBalance(accountNumber, amount.negate());
        return Result.success("取款成功");
    }

    @PostMapping("/transfer")
    public Result<String> transfer(@RequestBody Map<String, Object> params) {
        String from = (String) params.get("from");
        String to = (String) params.get("to");
        BigDecimal amount = new BigDecimal(params.get("amount").toString());

        bankService.transfer(from, to, amount);
        return Result.success("转账成功！");
    }

    @DeleteMapping("/{accountNumber}")
    public Result<String> deleteAccount(@PathVariable String accountNumber) {
        accountService.deleteAccount(accountNumber);
        return Result.success("销户成功");
    }

    @GetMapping("/{accountNumber}/transactions")
    public Result<List<Transaction>> getHistory(@PathVariable String accountNumber) {
        return Result.success(transactionService.getHistory(accountNumber));
    }
}