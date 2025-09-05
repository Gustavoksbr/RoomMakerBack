package com.example.roommaker.app.domain.exceptions;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String usernameOrEmail) {
        super("Username " + usernameOrEmail + " already exists.");
    }
}