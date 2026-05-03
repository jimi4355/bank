package com.example.bank.controller;

import com.example.bank.entity.User;
import com.example.bank.service.UserService;
import com.example.bank.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils; // 注入工具类

    /**
     * 用户注册接口
     * 对应 Postman URL: http://localhost:8080/user/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> params) {
        // 为了快速测试，我们直接用 Map 接收参数，之后再优化为 DTO
        String username = params.get("username");
        String password = params.get("password");
        String realName = params.get("realName");

        User user = userService.register(username, password, realName);

        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "注册成功",
                "username", user.getUsername()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        // 1. 验证用户名密码（逻辑在 Service）
        userService.login(username, password);

        // 2. 验证通过，生成 Token
        String token = jwtUtils.generateToken(username);

        // 3. 返回给前端（Postman）
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "登录成功",
                "token", token  // 这就是用户以后的通行证
        ));
    }
}