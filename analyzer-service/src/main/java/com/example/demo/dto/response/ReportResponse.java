package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReportResponse {
    private String namaFile;
    private int totalData;
    private String linkDownload;
}
