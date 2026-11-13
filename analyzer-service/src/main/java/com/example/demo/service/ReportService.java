package com.example.demo.service;

import com.example.demo.dto.response.ReportResponse;

public interface ReportService {
    ReportResponse buatLaporanHarian();
    String linkDownload(String namaFile);
}
