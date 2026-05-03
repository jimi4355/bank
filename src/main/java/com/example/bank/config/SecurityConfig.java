package com.example.bank.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;    //注入保安

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 会自动加盐（Salt），即使两个用户密码相同，数据库里的密文也不同
        return new BCryptPasswordEncoder();
    }

    // 在 SecurityConfig 类中添加
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 必须禁用 CSRF 才能支持 WebSocket 握手
                .authorizeHttpRequests(auth -> auth
                        // 1. 在这里添加 WebSocket 的放行路径
                        .requestMatchers("/user/register", "/user/login", "/bank-websocket/**").permitAll()
                        // 2. 这里的 anyRequest 包括了所有没在上面定义的路径，都需要 Token
                        .anyRequest().authenticated()
                )
                // 3. 将 JWT 过滤器放在标准验证过滤器之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
