package com.example.analyze.controller;

import com.example.analyze.dto.response.BaseResponse;
import com.example.analyze.dto.response.ReportResponse;
import com.example.analyze.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor(onConstructor = @__( @Autowired))
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @PostMapping("/generate")
    public BaseResponse<ReportResponse> generate() {
        return BaseResponse.ok(reportService.buatLaporanHarian());
    }

    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/{namaFile}/url")
    public BaseResponse<String> link(@PathVariable String namaFile) {
        return BaseResponse.ok(reportService.linkDownload(namaFile));
    }
}
