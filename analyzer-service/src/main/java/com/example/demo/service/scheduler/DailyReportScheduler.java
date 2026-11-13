package com.example.demo.service.scheduler;

import com.example.demo.dto.response.CharAnalysisResponse;
import com.example.demo.dto.response.ReportResponse;
import com.example.demo.service.CharAnalysisService;
import com.example.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__( @Autowired))
public class DailyReportScheduler {

    private final ReportService reportService;
    private final CharAnalysisService charAnalysisService;

    @Scheduled(cron = "${report.scheduler.cron}")
    public void jalankan() {
        log.info("[Scheduler] Mulai membuat laporan harian");
        try {
            CharAnalysisResponse quote = charAnalysisService.analisisDariQuote();
            log.info("[Scheduler] Quote baru tersimpan, id={}", quote.getId());

            ReportResponse laporan = reportService.buatLaporanHarian();
            log.info("[Scheduler] Laporan selesai: {} ({} data)", laporan.getNamaFile(), laporan.getTotalData());
        } catch (Exception e) {
            log.error("[Scheduler] Gagal: {}", e.getMessage());
        }
    }
}
