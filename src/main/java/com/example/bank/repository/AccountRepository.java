package com.example.bank.repository;

import com.example.bank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // 手动定义一个方法：根据账号查询
    // 只要方法名符合命名规范，Spring Data JPA 会自动帮你生成 SQL
    Optional<Account> findByAccountNumber(String accountNumber);
}