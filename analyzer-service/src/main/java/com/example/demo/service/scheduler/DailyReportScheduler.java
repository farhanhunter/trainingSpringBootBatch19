package com.example.demo.service.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DailyReportScheduler {

    @Scheduled(cron = "${report.scheduler.cron}")
    public void jalankan() {
        log.info("[Scheduler][{}] Alarm berbunyi", Thread.currentThread().getName());
    }
}
