package com.sqlearn.controller;

import com.sqlearn.dto.ApiResponse;
import com.sqlearn.dto.UserDto;
import com.sqlearn.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> me(@RequestAttribute("userId") Long userId) {
        return ApiResponse.ok(authService.getMe(userId));
    }
}
