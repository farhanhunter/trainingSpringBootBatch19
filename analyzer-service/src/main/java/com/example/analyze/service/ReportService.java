package com.example.analyze.service;

import com.example.analyze.dto.response.ReportResponse;

public interface ReportService {
    ReportResponse buatLaporanHarian();
    String linkDownload(String namaFile);
}
