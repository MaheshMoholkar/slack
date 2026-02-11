package com.slack.server.controller;

import com.slack.server.model.User;
import com.slack.server.dto.UserDTO;
import com.slack.server.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody @Valid RegisterRequest request) {
        User user = authService.register(
            request.getName(),
            request.getEmail(),
            request.getPassword()
        );
        // Auto-login after registration: generate token and return LoginResponse
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new LoginResponse(token, UserDTO.fromEntity(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(new LoginResponse(token, UserDTO.fromEntity(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();
            return ResponseEntity.ok(UserDTO.fromEntity(user));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @RequestBody @Valid UpdateProfileRequest request) {
        User user = authService.updateProfile(
            request.getUserId(),
            request.getName(),
            request.getImageUrl()
        );
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest request) {
        authService.changePassword(
            request.getUserId(),
            request.getCurrentPassword(),
            request.getNewPassword()
        );
        return ResponseEntity.ok().build();
    }

    public static class RegisterRequest {
        private @NonNull String name = "";
        private @NonNull String email = "";
        private @NonNull String password = "";

        public @NonNull String getName() { return name; }
        public void setName(@NonNull String name) { this.name = name; }

        public @NonNull String getEmail() { return email; }
        public void setEmail(@NonNull String email) { this.email = email; }

        public @NonNull String getPassword() { return password; }
        public void setPassword(@NonNull String password) { this.password = password; }
    }

    public static class LoginRequest {
        private @NonNull String email = "";
        private @NonNull String password = "";

        public @NonNull String getEmail() { return email; }
        public void setEmail(@NonNull String email) { this.email = email; }

        public @NonNull String getPassword() { return password; }
        public void setPassword(@NonNull String password) { this.password = password; }
    }

    public static class LoginResponse {
        private String token;
        private UserDTO user;

        public LoginResponse(String token, UserDTO user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public UserDTO getUser() { return user; }
        public void setUser(UserDTO user) { this.user = user; }
    }

    public static class UpdateProfileRequest {
        private @NonNull String userId = "";
        private @NonNull String name = "";
        private @Nullable String imageUrl;

        public @NonNull String getUserId() { return userId; }
        public void setUserId(@NonNull String userId) { this.userId = userId; }

        public @NonNull String getName() { return name; }
        public void setName(@NonNull String name) { this.name = name; }

        public @Nullable String getImageUrl() { return imageUrl; }
        public void setImageUrl(@Nullable String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class ChangePasswordRequest {
        private @NonNull String userId = "";
        private @NonNull String currentPassword = "";
        private @NonNull String newPassword = "";

        public @NonNull String getUserId() { return userId; }
        public void setUserId(@NonNull String userId) { this.userId = userId; }

        public @NonNull String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(@NonNull String currentPassword) { this.currentPassword = currentPassword; }

        public @NonNull String getNewPassword() { return newPassword; }
        public void setNewPassword(@NonNull String newPassword) { this.newPassword = newPassword; }
    }
}