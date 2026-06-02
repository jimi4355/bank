package com.example.bank.service;

import com.example.bank.entity.User;
import com.example.bank.exception.BusinessException;
import com.example.bank.repository.UserRepository;
import com.example.bank.dto.RealNameAuthDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public User register(String username, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(4001, "该用户名已被占用");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        // 初始状态明确
        user.setAuthStatus(User.AuthStatus.UNVERIFIED);

        return userRepository.save(user);
    }

    public User login(String username, String password) {
        // 统一错误信息，防止用户名探测
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(4002, "用户名或密码无效"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(4002, "用户名或密码无效");
        }

        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitRealNameAuth(Long userId, RealNameAuthDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(4004, "用户不存在"));

        // 1. 基础校验：是否已经认证过
        if (user.getAuthStatus() == User.AuthStatus.VERIFIED) {
            throw new BusinessException(4005, "该用户已完成实名认证");
        }

        // 2. 逻辑校验：身份证号唯一性
        if (userRepository.existsByIdCardNumber(dto.getIdCardNumber())) {
            throw new BusinessException(4006, "该身份证号已被其他账号绑定");
        }

        // 3. 模拟身份核验接口（在此处你可以接入第三方 OCR 或公安部接口，现在先手动模拟）
        boolean isIdValid = simulateExternalIdCheck(dto.getRealName(), dto.getIdCardNumber());

        if (isIdValid) {
            user.setRealName(dto.getRealName());
            user.setIdCardNumber(dto.getIdCardNumber());
            user.setAuthStatus(User.AuthStatus.VERIFIED);
            user.setAuthTime(LocalDateTime.now());
            userRepository.save(user);
            log.info("用户 {} 实名认证成功", userId);
        } else {
            throw new BusinessException(4007, "身份证信息核验失败");
        }
    }

    private boolean simulateExternalIdCheck(String name, String id) {
        // 模拟逻辑：名字不能为空，且身份证号长度为18位
        return name != null && id != null && id.length() == 18;
    }
}
