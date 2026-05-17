package com.example.bank.repository;

import com.example.bank.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 查询某个账号的所有流水（无论是转出还是转入）
     */
    List<Transaction> findByFromAccountOrToAccountOrderByTimestampDesc(String fromAccount, String toAccount);

    /**
     * 分页查询流水，防止数据量过大导致 OOM
     */
    Page<Transaction> findByFromAccountOrToAccount(String fromAccount, String toAccount, Pageable pageable);
}