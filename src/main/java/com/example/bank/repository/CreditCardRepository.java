package com.example.bank.repository;

import com.example.bank.entity.CreditCard;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    /**
     * 修改为 List，因为一个用户可以拥有多张信用卡
     */
    List<CreditCard> findAllByUserId(Long userId);

    // 只需要添加这一行
    // JPA 会自动解析为：SELECT * FROM credit_cards WHERE billing_date = ?
    List<CreditCard> findByBillingDate(Integer billingDate);

    /**
     * 查找绑定了特定储蓄账户的所有信用卡
     * 用于储蓄卡注销前的关联检查
     */
    List<CreditCard> findAllByDefaultRepayAccount(String accountNo);

    /**
     * 带有行级锁的查询
     * 执行此查询时，Postgres 会执行 SELECT ... FOR UPDATE
     * 其他线程必须等待当前事务完成才能读取/修改这行数据
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")}) // 等待3秒，拿不到锁就抛异常
    @Query("SELECT c FROM CreditCard c WHERE c.cardNumber = :cardNumber")
    Optional<CreditCard> findByCardNumberWithLock(String cardNumber);

    // 查找状态不是 NORMAL 的卡片
    List<CreditCard> findAllByStatusNot(String status);

    // 查找属于某些特定状态的卡片
    List<CreditCard> findAllByStatusIn(List<String> statuses);
}