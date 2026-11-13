package com.example.demo.controller;

import com.example.demo.dto.response.BaseResponse;
import com.example.demo.dto.response.ReportResponse;
import com.example.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor(onConstructor = @__( @Autowired))
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    public BaseResponse<ReportResponse> generate() {
        return BaseResponse.ok(reportService.buatLaporanHarian());
    }

    @GetMapping("/{namaFile}/url")
    public BaseResponse<String> link(@PathVariable String namaFile) {
        return BaseResponse.ok(reportService.linkDownload(namaFile));
    }
}
