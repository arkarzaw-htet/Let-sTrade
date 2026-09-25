package com.letsTrade.demo.user.controller;


import com.letsTrade.demo.user.dto.UserResponse;
import com.letsTrade.demo.user.entity.User;
import com.letsTrade.demo.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {

        User user = userService.getUserByEmail(
                authentication.getName()
        );

        return new UserResponse(user);
    }
}