package com.cropwise.backend.service;

import com.cropwise.backend.dto.AuthDtos.*;
import com.cropwise.backend.dto.UserDto;
import com.cropwise.backend.model.User;
import com.cropwise.backend.repository.UserRepository;
import com.cropwise.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest req) {
        if (users.existsByEmail(req.email().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User u = new User();
        u.setEmail(req.email().toLowerCase());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setName(req.name());
        users.save(u);
        String token = jwt.generateToken(u.getId(), u.getEmail());
        return new AuthResponse(token, UserDto.from(u));
    }

    public AuthResponse login(LoginRequest req) {
        User u = users.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String token = jwt.generateToken(u.getId(), u.getEmail());
        return new AuthResponse(token, UserDto.from(u));
    }
}
