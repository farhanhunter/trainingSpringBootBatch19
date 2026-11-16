package com.example.analyze.service;

import com.example.analyze.dto.request.AuthRequestDto;
import com.example.analyze.dto.response.AuthResponseDto;
import com.example.analyze.entity.UserEntity;

public interface AuthService {
    String register(AuthRequestDto authRequestDto);
    AuthResponseDto login(AuthRequestDto authRequestDto);
    UserEntity loadUserByUsername(String username);
}
