package com.example.analyze.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AuthResponseDto {

    private String username;

    private String token;

    private List<String> roles;
}
