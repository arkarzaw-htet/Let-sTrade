package com.letsTrade.demo.user.service;

import com.letsTrade.demo.common.exception.ResourceNotFoundException;
import com.letsTrade.demo.user.entity.User;
import com.letsTrade.demo.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with email: " + email)
                );
    }
}