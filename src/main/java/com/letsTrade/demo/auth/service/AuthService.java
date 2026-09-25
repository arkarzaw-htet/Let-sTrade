package com.letsTrade.demo.auth.service;

import com.letsTrade.demo.auth.dto.LoginRequest;
import com.letsTrade.demo.auth.dto.RegisterRequest;
import com.letsTrade.demo.auth.security.JwtService;
import com.letsTrade.demo.user.entity.User;
import com.letsTrade.demo.user.repository.UserRepository;
import com.letsTrade.demo.wallet.service.WalletService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletService walletService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            WalletService walletService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.walletService = walletService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getName(),
                email,
                hashedPassword
        );

        User savedUser = userRepository.save(user);
        walletService.createInitialWallet(savedUser);
    }

    public String login(LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new RuntimeException("Invalid email or password");
        }

        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password")
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new RuntimeException("Invalid email or password");
        }

        return jwtService.generateToken(user.getEmail());
    }
}