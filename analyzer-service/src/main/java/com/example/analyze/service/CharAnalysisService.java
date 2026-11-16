package com.example.analyze.service;

import com.example.analyze.dto.request.CharAnalysisRequest;
import com.example.analyze.dto.response.CharAnalysisResponse;
import com.example.analyze.dto.response.QuotePageAnalysisResponse;
import com.example.analyze.dto.response.TrendingResponse;

import java.util.List;

public interface CharAnalysisService {
    List<CharAnalysisResponse> cari(String kata);

    CharAnalysisResponse analisis(CharAnalysisRequest request);

    CharAnalysisResponse cariById(Long id);

    List<CharAnalysisResponse> cariMinimalHuruf(int min);

    List<CharAnalysisResponse> limaTerbaru();

    CharAnalysisResponse analisisDariQuote();


    CharAnalysisResponse analysisQuoteById(Long id);

    List<CharAnalysisResponse> analisisBanyakQuote(int jumlah);

    QuotePageAnalysisResponse analisisHalamanQuote(int limit, int skip);

    List<CharAnalysisResponse> analysisDoubleQuote();

    List<TrendingResponse> trending(int limit);
}
