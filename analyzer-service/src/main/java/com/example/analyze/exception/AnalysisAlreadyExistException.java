package com.example.analyze.exception;

public class AnalysisAlreadyExistException extends RuntimeException {
    public AnalysisAlreadyExistException(String kalimat) {
        super("Kalimat '" + kalimat + "' sudah pernah dianalisis");
    }
}
