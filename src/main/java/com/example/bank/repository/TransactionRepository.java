package com.example.bank.repository;

import com.example.bank.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    // 根据卡号、流水类型、时间范围 统计总消费金额
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.fromAccount = :cardNo " +
            "AND t.type = 'WITHDRAW' AND t.timestamp BETWEEN :start AND :end")
    BigDecimal sumMonthConsumption(@Param("cardNo") String cardNo,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

}

