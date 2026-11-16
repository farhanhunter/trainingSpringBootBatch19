package com.example.analyze.controller;

import com.example.analyze.dto.request.CharAnalysisRequest;
import com.example.analyze.dto.response.BaseResponse;
import com.example.analyze.dto.response.CharAnalysisResponse;
import com.example.analyze.dto.response.QuotePageAnalysisResponse;
import com.example.analyze.dto.response.TrendingResponse;
import com.example.analyze.service.CharAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor(onConstructor = @__( @Autowired))
public class AnalyzerController {

    private final CharAnalysisService charAnalysisService;

    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping
    public BaseResponse<List<CharAnalysisResponse>> cari(
            @RequestParam(required = false) String kalimat) {
        return BaseResponse.ok(charAnalysisService.cari(kalimat));
    }

    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @PostMapping
    public BaseResponse<CharAnalysisResponse> analisis(
            @Valid @RequestBody CharAnalysisRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        log.info("user: {}, kalimat: {}", userId, request.getKalimat());
        return BaseResponse.ok(charAnalysisService.analisis(request));
    }

    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/{id}")
    public BaseResponse<CharAnalysisResponse> detail(@PathVariable Long id) {
        CharAnalysisResponse hasil = charAnalysisService.cariById(id);
        return BaseResponse.ok(hasil);
    }

    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/minimal")
    public BaseResponse<List<CharAnalysisResponse>> minimal(@RequestParam int min) {
        return BaseResponse.ok(charAnalysisService.cariMinimalHuruf(min));
    }

    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/terbaru")
    public BaseResponse<List<CharAnalysisResponse>> terbaru() {
        return BaseResponse.ok(charAnalysisService.limaTerbaru());
    }

    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @PostMapping("/from-quote")
    public BaseResponse<CharAnalysisResponse> fromQuote() {
        return BaseResponse.ok(charAnalysisService.analisisDariQuote());
    }

    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @PostMapping("/from-quote/{id}")
    public BaseResponse<CharAnalysisResponse> fromQuoteById(@PathVariable Long id) {
        return BaseResponse.ok(charAnalysisService.analysisQuoteById(id));
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @PostMapping("/from-quote/batch/{jumlah}")
    public BaseResponse<List<CharAnalysisResponse>> fromBanyakQuote(@PathVariable int jumlah) {
        return BaseResponse.ok(charAnalysisService.analisisBanyakQuote(jumlah));
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @PostMapping("/from-quote/page")
    public BaseResponse<QuotePageAnalysisResponse> fromHalamanQuote(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "0") int skip) {
        return BaseResponse.ok(charAnalysisService.analisisHalamanQuote(limit, skip));
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @PostMapping("/from-qoute/double")
    public BaseResponse<List<CharAnalysisResponse>> fromDoubleQuote() {
        return BaseResponse.ok(charAnalysisService.analysisDoubleQuote());
    }

    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/trending")
    public BaseResponse<List<TrendingResponse>> trending(@RequestParam(defaultValue = "5") int limit) {
        return BaseResponse.ok(charAnalysisService.trending(limit));
    }
}
