package com.brodep.apigatewayservice.service;

import com.brodep.apigatewayservice.entity.User;
import com.brodep.apigatewayservice.exeption.ResourceNotFoundException;
import com.brodep.apigatewayservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean existsByUserName(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User with username %s not found".formatted(username)));
    }

    @Transactional(readOnly = true)
    public UserDetailsService userDetailsService() {
        return this::findByUsername;
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username);
    }
}
