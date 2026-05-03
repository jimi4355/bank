package com.example.bank.repository;

import com.example.bank.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
    // 可以在这里扩展：根据账号查询流水列表
}