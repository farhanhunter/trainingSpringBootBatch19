package com.example.analyze.exception;

public class QuoteNotFoundException extends RuntimeException {
    public QuoteNotFoundException(Long id) {
        super("Quote dengan id " + id + " tidak ditemukan");
    }
}
