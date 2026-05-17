package com.example.bank.repository;

import com.example.bank.entity.CreditCard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    /**
     * 根据卡号查询信用卡
     * 用于消费、还款时的卡片定位
     */
    Optional<CreditCard> findByCardNumber(String cardNumber);

    /**
     * 根据用户 ID 查询名下的信用卡
     * 一个用户可能有多张卡
     */
    Optional<CreditCard> findByUserId(Long userId);

    /**
     * 带有行级锁的查询
     * 执行此查询时，Postgres 会执行 SELECT ... FOR UPDATE
     * 其他线程必须等待当前事务完成才能读取/修改这行数据
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CreditCard c WHERE c.cardNumber = :cardNumber")
    Optional<CreditCard> findByCardNumberWithLock(String cardNumber);
}