package com.example.bank.repository;

import com.example.bank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户
     * 用于登录认证逻辑
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查身份证号是否已在系统中使用
     * 对应 UserService 中的 existsByIdCardNumber 逻辑
     */
    boolean existsByIdCardNumber(String idCardNumber);

    /**
     * 根据手机号查询用户
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * 根据身份证号查询用户
     */
    Optional<User> findByIdCardNumber(String idCardNumber);

    /**
     * 根据认证状态批量查询
     * 例如：查询所有 AuthStatus.VERIFIED 的用户
     */
    List<User> findAllByAuthStatus(User.AuthStatus authStatus);
}