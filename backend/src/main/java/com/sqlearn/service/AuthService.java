package com.sqlearn.service;

import com.sqlearn.dto.AuthResponse;
import com.sqlearn.dto.LoginRequest;
import com.sqlearn.dto.RegisterRequest;
import com.sqlearn.dto.UserDto;
import com.sqlearn.entity.User;
import com.sqlearn.exception.BizException;
import com.sqlearn.repository.UserRepository;
import com.sqlearn.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BizException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BizException("该邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getUsername()),
                UserDto.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseGet(() -> userRepository.findByEmail(request.username()).orElse(null));

        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }

        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getUsername()),
                UserDto.from(user));
    }

    public UserDto getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));
        return UserDto.from(user);
    }
}
