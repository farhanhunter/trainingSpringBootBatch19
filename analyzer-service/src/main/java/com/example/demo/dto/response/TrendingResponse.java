package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrendingResponse {
    private String huruf;
    private Long jumlah;
}
