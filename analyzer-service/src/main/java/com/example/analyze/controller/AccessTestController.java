package com.example.analyze.controller;

import com.example.analyze.dto.response.BaseResponse;
import com.example.analyze.security.UserDetailsImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint uji role, sama seperti TestController di bezkoder */
@RestController
@RequestMapping("/api/v1/test")
public class AccessTestController {

    @GetMapping("/me")
    public BaseResponse<Object> me(@AuthenticationPrincipal UserDetailsImpl user) {
        return BaseResponse.ok(user.getUsername() + " -> " + user.getAuthorities());
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    public BaseResponse<String> userAccess() {
        return BaseResponse.ok("User Content");
    }

    @GetMapping("/mod")
    @PreAuthorize("hasRole('MODERATOR')")
    public BaseResponse<String> moderatorAccess() {
        return BaseResponse.ok("Moderator Board");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<String> adminAccess() {
        return BaseResponse.ok("Admin Board");
    }
}
