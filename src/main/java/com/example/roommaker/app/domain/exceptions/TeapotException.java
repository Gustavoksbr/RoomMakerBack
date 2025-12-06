package com.example.roommaker.app.domain.exceptions;

public class TeapotException extends RuntimeException {
    public TeapotException() {
        super("I'm a teapot");
    }
}
