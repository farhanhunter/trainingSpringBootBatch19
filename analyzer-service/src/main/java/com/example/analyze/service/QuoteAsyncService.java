package com.example.analyze.service;

import com.example.analyze.dto.feign.QuoteResponse;

import java.util.concurrent.CompletableFuture;

public interface QuoteAsyncService {
    CompletableFuture<QuoteResponse> getQuote();
}
