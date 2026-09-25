package com.letsTrade.demo.auth.controller;



import com.letsTrade.demo.auth.dto.LoginRequest;
import com.letsTrade.demo.auth.dto.LoginResponse;
import com.letsTrade.demo.auth.dto.RegisterRequest;
import com.letsTrade.demo.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody RegisterRequest request) {

        authService.register(request);
    }
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        String token = authService.login(request);

        return new LoginResponse(token);
    }
}
