package com.example.analyze.service;

import com.example.analyze.dto.request.AuthRequestDto;
import com.example.analyze.dto.response.AuthResponseDto;

public interface AuthService {
    String register(AuthRequestDto authRequestDto);
    AuthResponseDto login(AuthRequestDto authRequestDto);
}
