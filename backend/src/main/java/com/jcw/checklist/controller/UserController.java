package com.jcw.checklist.controller;

import com.jcw.checklist.model.User;
import com.jcw.checklist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private Environment environment;

      @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest request) {
        // Check if we're in dev profile
        if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            // In dev mode, check for selected user in header or default to alice
            String selectedUser = request.getHeader("X-Dev-User");
            
            if (selectedUser == null || selectedUser.isEmpty()) {
                selectedUser = "alice";
            }
            
            final String finalSelectedUser = selectedUser; // For lambda usage
            
            // Return the selected user's actual data from the database
            return userRepository.findByUsername(finalSelectedUser)
                    .orElseGet(() -> {
                        // Fallback to alice if selected user not found
                        return userRepository.findByUsername("alice")
                                .orElseGet(() -> {
                                    User alice = new User();
                                    alice.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
                                    alice.setUsername("alice");
                                    return userRepository.save(alice);
                                });
                    });
        }

    // Production OAuth2 handling
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    String username;
    String email = null;

    if (authentication.getPrincipal() instanceof OAuth2User) {
      OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
      // Get Google user info
      email = oauth2User.getAttribute("email");
      username = oauth2User.getAttribute("name");

      if (username == null) username = email;
    } else {
      username = authentication.getName();
    }

    // Find or create user based on OAuth2 info
    String finalUsername = username;
    return userRepository.findByUsername(username)
        .orElseGet(() -> {
          User newUser = new User();
          newUser.setId(UUID.randomUUID());
          newUser.setUsername(finalUsername);
          return userRepository.save(newUser);
        });
  }

  @GetMapping
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }
} 