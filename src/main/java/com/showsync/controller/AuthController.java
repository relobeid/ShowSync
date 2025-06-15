package com.showsync.controller;

import com.showsync.dto.AuthRequest;
import com.showsync.dto.AuthResponse;
import com.showsync.dto.RegisterRequest;
import com.showsync.entity.User;
import com.showsync.security.UserPrincipal;
import com.showsync.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get the current authenticated user's profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User user = userPrincipal.getUser();
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("username", user.getUsername());
            profile.put("email", user.getEmail());
            profile.put("displayName", user.getDisplayName());
            profile.put("profilePictureUrl", user.getProfilePictureUrl());
            profile.put("bio", user.getBio());
            profile.put("role", user.getRole().name());
            profile.put("emailVerified", user.isEmailVerified());
            profile.put("createdAt", user.getCreatedAt());
            profile.put("lastLoginAt", user.getLastLoginAt());
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get user profile");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update the current authenticated user's profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                         @RequestBody Map<String, Object> updates) {
        try {
            User user = userPrincipal.getUser();
            
            // Update allowed fields
            if (updates.containsKey("displayName")) {
                user.setDisplayName((String) updates.get("displayName"));
            }
            if (updates.containsKey("bio")) {
                user.setBio((String) updates.get("bio"));
            }
            if (updates.containsKey("profilePictureUrl")) {
                user.setProfilePictureUrl((String) updates.get("profilePictureUrl"));
            }
            
            // Save and return updated profile
            User updatedUser = authService.updateUserProfile(user);
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", updatedUser.getId());
            profile.put("username", updatedUser.getUsername());
            profile.put("email", updatedUser.getEmail());
            profile.put("displayName", updatedUser.getDisplayName());
            profile.put("profilePictureUrl", updatedUser.getProfilePictureUrl());
            profile.put("bio", updatedUser.getBio());
            profile.put("role", updatedUser.getRole().name());
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update user profile");
            return ResponseEntity.badRequest().body(error);
        }
    }
} 