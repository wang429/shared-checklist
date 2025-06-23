package com.jcw.checklist.service;

import com.jcw.checklist.model.User;
import com.jcw.checklist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Create UserDetails from database user
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password("password") // Plain text password since we're using NoOpPasswordEncoder
                .roles(determineRoles(user.getUsername()))
                .build();
    }

    private String[] determineRoles(String username) {
        // Simple role assignment - in real app this would come from database
        if ("admin".equals(username)) {
            return new String[]{"USER", "ADMIN"};
        }
        return new String[]{"USER"};
    }
} 