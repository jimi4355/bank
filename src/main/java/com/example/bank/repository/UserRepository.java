package com.example.bank.repository;

import com.example.bank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户
     * 用于登录认证逻辑
     * @param username 登录名
     * @return 包含用户信息的 Optional
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在
     * 用于注册时的唯一性校验
     * @param username 登录名
     * @return true 表示已存在，false 表示可用
     */
    boolean existsByUsername(String username);

    /**
     * 根据手机号查询用户
     * 金融系统通常支持手机号登录或找回密码
     * @param phoneNumber 手机号
     * @return 包含用户信息的 Optional
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
}