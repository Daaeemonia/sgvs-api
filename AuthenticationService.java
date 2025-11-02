package com.flick.business.service.security;

import com.flick.business.api.dto.auth.AuthResponse;
import com.flick.business.api.dto.auth.LoginRequest;
import com.flick.business.api.dto.auth.RegisterRequest;
import com.flick.business.core.entity.security.User;
import com.flick.business.core.enums.security.Role;
import com.flick.business.exception.BusinessException;
import com.flick.business.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the system.
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("Username already exists: " + request.getUsername());
        }

        // Create new user
        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        // Save user
        User savedUser = userRepository.save(user);

        // Generate JWT token
        var jwtToken = jwtService.generateToken(savedUser);

        // Return authentication response
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Authenticates a user and returns a JWT token.
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate user credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // If authentication succeeds, load user and generate token
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}
