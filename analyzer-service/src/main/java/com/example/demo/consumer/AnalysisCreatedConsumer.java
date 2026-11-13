package com.example.demo.consumer;

import com.example.demo.util.CharCounter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AnalysisCreatedConsumer {
    public static final String TRENDING_KEY = "analyzer:huruf:trending";

    private final StringRedisTemplate stringRedisTemplate;

    @KafkaListener(
            topics = "${kafka.topic.analysis-created}",
            groupId = "analyzer-trending"
    )
    public void onAnalysisCreated(String kalimat) {
        log.info("Consume Kalimat: {}", kalimat);

        CharCounter counter = new CharCounter(kalimat);
        counter.toMap().forEach((hurufKey, hurufValue) -> {
            stringRedisTemplate.opsForZSet()
                    .incrementScore(TRENDING_KEY, String.valueOf(hurufKey), hurufValue);
        });
    }
}
