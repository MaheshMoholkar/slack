package com.slack.server.service.impl;

import com.slack.server.model.User;
import com.slack.server.repository.UserRepository;
import com.slack.server.security.JwtTokenProvider;
import com.slack.server.service.AuthService;
import com.slack.server.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public User register(String name, String email, String password) {
        if (userRepository.existsByEmail(java.util.Objects.requireNonNull(email))) {
            throw new IllegalStateException("Email is already registered");
        }

        User user = new User();
        user.setName(java.util.Objects.requireNonNull(name));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(java.util.Objects.requireNonNull(password)));
        return userRepository.save(user);
    }

    @Override
    public String login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(java.util.Objects.requireNonNull(email), java.util.Objects.requireNonNull(password))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Update user presence
        User user = getCurrentUser();
        if (user != null) {
            webSocketService.updatePresence(user.getId(), true);
        }

        return tokenProvider.generateToken(authentication);
    }

    @Override
    public void logout() {
        User user = getCurrentUser();
        SecurityContextHolder.clearContext();
        if (user != null) {
            webSocketService.updatePresence(user.getId(), false);
        }
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authentication found");
        }
        return userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    @Override
    public User updateProfile(String userId, String name, @Nullable String imageUrl) {
        User user = userRepository.findById(java.util.Objects.requireNonNull(userId))
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setName(java.util.Objects.requireNonNull(name));
        if (imageUrl != null) {
            user.setImageUrl(imageUrl);
        }
        return userRepository.save(user);
    }

    @Override
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(java.util.Objects.requireNonNull(userId))
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(java.util.Objects.requireNonNull(currentPassword), user.getPassword())) {
            throw new IllegalStateException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(java.util.Objects.requireNonNull(newPassword)));
        userRepository.save(user);
    }

    @Override
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(java.util.Objects.requireNonNull(token));
    }
}