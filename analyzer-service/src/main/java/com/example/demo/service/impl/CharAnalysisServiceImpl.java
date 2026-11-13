package com.example.demo.service.impl;

import com.example.demo.consumer.AnalysisCreatedConsumer;
import com.example.demo.dto.feign.QuoteListResponse;
import com.example.demo.dto.feign.QuoteResponse;
import com.example.demo.dto.request.CharAnalysisRequest;
import com.example.demo.dto.response.CharAnalysisResponse;
import com.example.demo.dto.response.QuotePageAnalysisResponse;
import com.example.demo.dto.response.TrendingResponse;
import com.example.demo.entity.CharAnalysis;
import com.example.demo.exception.*;
import com.example.demo.producer.KafkaProducer;
import com.example.demo.repository.CharAnalysisRepository;
import com.example.demo.rest.QuoteClient;
import com.example.demo.service.CharAnalysisService;
import com.example.demo.service.QuoteAsyncService;
import com.example.demo.service.RiwayatService;
import com.example.demo.util.CharCounter;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CharAnalysisServiceImpl implements CharAnalysisService {

    private final QuoteClient client;
    private final CharAnalysisRepository charAnalysisRepository;
    private final RiwayatService riwayatService;
    private final QuoteAsyncService quoteAsyncService;
    private final KafkaProducer kafkaProducer;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${kafka.topic.analysis-created}")
    private String topic;

    @Override
    public List<CharAnalysisResponse> cari(String kata) {
        List<CharAnalysis> semua;
        if (kata == null || kata.isBlank()) {
            semua = charAnalysisRepository.findAll();
        } else {
            semua = charAnalysisRepository.findByKalimatContainingIgnoreCase(kata);
        }

        List<CharAnalysisResponse> hasil = new ArrayList<>();
        for (CharAnalysis data : semua) {
            hasil.add(toResponse(data));
        }
        return hasil;
    }

    @Override
    @CacheEvict(value = "analysis", allEntries = true)
    public CharAnalysisResponse analisis(CharAnalysisRequest request) {
        String kalimat = request.getKalimat().trim();
        if (charAnalysisRepository.existsByKalimatIgnoreCase(kalimat)) {
            throw new AnalysisAlreadyExistException(kalimat);
        }

        return simpan(kalimat, null);
    }

    @Override
    @Cacheable(value = "analysis", key = "#id")
    public CharAnalysisResponse cariById(Long id) {
        log.info("Caching CharAnalysis with id: {}", id);
        CharAnalysis data = charAnalysisRepository.findById(id)
                .orElseThrow(() -> new AnalysisNotFoundException(id));
        return toResponse(data);
    }

    @Override
    public List<CharAnalysisResponse> cariMinimalHuruf(int min) {
        return toResponseList(charAnalysisRepository.cariMinimalHuruf(min));
    }

    @Override
    public List<CharAnalysisResponse> limaTerbaru() {
        return toResponseList(charAnalysisRepository.limaTerbaru());
    }

    @Override
    public CharAnalysisResponse analisisDariQuote() {
        QuoteResponse quote;
        try {
            quote = client.getQuote();
        } catch (FeignException feignException) {
            log.error("Feign ke quote-service gagal: status={}, pesan={}",
                    feignException.status(), feignException.getMessage());
            throw new QuoteUnavailableException("Service unavailable");
        }
        return simpan(quote.getQuote(), quote.getAuthor());
    }

    @Override
    public CharAnalysisResponse analysisQuoteById(Long id) {
        QuoteResponse quoteResponse;
        try {
            quoteResponse = client.getQuoteById(id);
        } catch (FeignException.NotFound feignException) {
            throw new QuoteNotFoundException(id);
        } catch (FeignException feignException) {
            throw new QuoteUnavailableException("Service unavailable");
        }
        log.info("Quote id={}: {} - {}", id, quoteResponse.getQuote(), quoteResponse.getAuthor());

        if (charAnalysisRepository.existsByKalimatIgnoreCase(quoteResponse.getQuote())) {
            throw new AnalysisAlreadyExistException(quoteResponse.getQuote());
        }
        return simpan(quoteResponse.getQuote(), quoteResponse.getAuthor());
    }

    @Override
    public List<CharAnalysisResponse> analisisBanyakQuote(int jumlah) {
        if (jumlah < 1 || jumlah > 10) {
            throw new InvalidJumlahException();
        }

        List<QuoteResponse> quotes;
        try {
            quotes = client.getRandomQuotes(jumlah);
        } catch (FeignException e) {
            throw new QuoteUnavailableException("Service unavailable");
        }

        List<CharAnalysisResponse> hasil = new ArrayList<>();
        for (QuoteResponse quote : quotes) {
            hasil.add(simpan(quote.getQuote(), quote.getAuthor()));
        }
        log.info("{} quote berhasil disimpan", hasil.size());
        return hasil;
    }

    @Override
    public QuotePageAnalysisResponse analisisHalamanQuote(int limit, int skip) {
        if (limit < 1 || limit > 20) {
            throw new InvalidPageException("Limit harus 1 sampai 20");
        }
        if (skip < 0) {
            throw new InvalidPageException("Skip tidak boleh negatif");
        }

        QuoteListResponse halaman;
        try {
            halaman = client.getQuotes(limit, skip);
        } catch (FeignException e) {
            throw new QuoteUnavailableException("Service unavailable");
        }

        List<CharAnalysisResponse> hasil = new ArrayList<>();
        for (QuoteResponse quote : halaman.getQuotes()) {
            hasil.add(simpan(quote.getQuote(), quote.getAuthor()));
        }
        log.info("Halaman skip={} limit={}: {} quote disimpan", skip, limit, hasil.size());

        return new QuotePageAnalysisResponse(halaman.getTotal(), halaman.getSkip(), halaman.getLimit(), hasil);

    }

    @Override
    public List<CharAnalysisResponse> analysisDoubleQuote() {
        long mulai = System.currentTimeMillis();

        CompletableFuture<QuoteResponse> quote1 = quoteAsyncService.getQuote();
        CompletableFuture<QuoteResponse> quote2 = quoteAsyncService.getQuote();

        List<CharAnalysisResponse> hasil = new ArrayList<>();
        try {
            QuoteResponse hasilqoute1 = quote1.join();
            QuoteResponse hasilqoute2 = quote2.join();
            hasil.add(simpan(hasilqoute1.getQuote(), hasilqoute1.getAuthor()));
            hasil.add(simpan(hasilqoute2.getQuote(), hasilqoute2.getAuthor()));
        } catch (CompletionException completionException) {
            throw new QuoteUnavailableException("Service unavailable");
        }
        log.info("[{}] dua quote selesai dalam {} ms",
                Thread.currentThread().getName(), System.currentTimeMillis() - mulai);
        return hasil;
    }

    @Override
    public List<TrendingResponse> trending(int limit) {
        Set<ZSetOperations.TypedTuple<String>> dataSet = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(AnalysisCreatedConsumer.TRENDING_KEY, 0, limit - 1L);

        List<TrendingResponse> results = new ArrayList<>();
        if (dataSet != null) {
            for (ZSetOperations.TypedTuple<String> data : dataSet) {
                long score = data.getScore() == null ? 0L : data.getScore().longValue();
                results.add(new TrendingResponse(data.getValue(), score));
            }
        }
        return results;
    }

    private CharAnalysisResponse simpan(String kalimat, String author) {
        CharCounter counter = new CharCounter(kalimat);
        CharAnalysis data = new CharAnalysis();

        data.setKalimat(kalimat);
        data.setAuthor(author);
        data.setTotalHuruf(counter.getTotalHuruf());
        data.setCreatedAt(LocalDateTime.now());
        charAnalysisRepository.save(data);
        kafkaProducer.sendMessage(topic, kalimat);

        riwayatService.catatRiwayat(data.getId(), kalimat);
        log.info("[{}] response dikirim ke client", Thread.currentThread().getName());

        return CharAnalysisResponse.from(data, counter.toMap());
    }

    private List<CharAnalysisResponse> toResponseList(List<CharAnalysis> semua) {
        List<CharAnalysisResponse> hasil = new ArrayList<>();
        for (CharAnalysis data : semua) {
            hasil.add(toResponse(data));
        }
        return hasil;
    }

    private CharAnalysisResponse toResponse(CharAnalysis data) {
        CharCounter counter = new CharCounter(data.getKalimat());
        return CharAnalysisResponse.from(data, counter.toMap());
    }
}
