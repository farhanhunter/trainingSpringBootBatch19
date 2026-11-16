package com.example.analyze.service.impl;

import com.example.analyze.dto.response.ReportResponse;
import com.example.analyze.entity.CharAnalysis;
import com.example.analyze.repository.CharAnalysisRepository;
import com.example.analyze.service.ReportService;
import com.example.analyze.util.MinioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__( @Autowired))
public class ReportServiceImpl implements ReportService {

    private final CharAnalysisRepository charAnalysisRepository;
    private final MinioUtil minioUtil;

    @Override
    public ReportResponse buatLaporanHarian() {
        LocalDateTime sampai = LocalDateTime.now();
        LocalDateTime dari = sampai.minusDays(1);
        List<CharAnalysis> data = charAnalysisRepository.findByCreatedAtBetweenOrderByIdAsc(dari, sampai);

        StringBuilder csv = new StringBuilder("id,kalimat,author,total_huruf,created_at\n");
        for (CharAnalysis a : data) {
            csv.append(a.getId()).append(',')
                    .append('"').append(a.getKalimat().replace("\"", "\"\"")).append('"').append(',')
                    .append(a.getAuthor() == null ? "" : a.getAuthor()).append(',')
                    .append(a.getTotalHuruf()).append(',')
                    .append(a.getCreatedAt()).append('\n');
        }

        String namaFile = "report-" + System.currentTimeMillis() + ".csv";
        minioUtil.upload(namaFile, csv.toString().getBytes(StandardCharsets.UTF_8), "text/csv");
        log.info("Laporan {} dibuat dengan {} data", namaFile, data.size());

        return new ReportResponse(namaFile, data.size(), minioUtil.linkDownload(namaFile));
    }

    @Override
    public String linkDownload(String namaFile) {
        return minioUtil.linkDownload(namaFile);
    }
}
