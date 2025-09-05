package com.example.roommaker.app.domain.ports.auth;

public interface JwtService {
    String generateToken(String subject);
    String getUsername(String token);
}