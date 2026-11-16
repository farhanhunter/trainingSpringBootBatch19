package com.example.analyze.exception;

public class UnauthrorizedException extends RuntimeException {
    public UnauthrorizedException(String message) {
        super(message);
    }
}
