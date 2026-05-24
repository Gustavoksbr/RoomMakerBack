package com.example.roommaker.app.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JwtResponse extends Response {

    private String token;

    private String username;

    private String email;

    public JwtResponse(String token) {
        this.token = token;
    }

    public JwtResponse(String token, String username, String email) {
        this.token = token;
        this.username = username;
        this.email = email;
    }


}