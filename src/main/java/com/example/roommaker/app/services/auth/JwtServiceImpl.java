package com.example.roommaker.app.services.auth;

import com.example.roommaker.app.domain.ports.auth.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;


import java.time.Instant;

@Service
public class JwtServiceImpl implements JwtService {

    @Autowired
    private  JwtEncoder jwtEncoder;
    @Autowired
    private JwtDecoder jwtDecoder;

@Override

    public String generateToken(String subject) {
        Instant now = Instant.now();
        long expiry = 36001L;
//    long expiry = 15L;
        var claims = JwtClaimsSet.builder()
                .issuer("spring-security-jwt")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(subject)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String getUsername(String token) {


            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return jwtDecoder.decode(token).getSubject(); // Decodifica o token e retorna o username

    }

}