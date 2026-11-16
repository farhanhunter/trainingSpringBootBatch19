package com.example.analyze.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.util.Set;

@Data
public class AuthRequestDto {

    @NotBlank(message = "username wajib diisi")
    private String username;

    @NotBlank(message = "Password wajib diisi")
    @Size(min = 8, message = "Password minimal 8 character")
    @ToString.Exclude
    private String password;

    // hanya dipakai saat register, opsional: ["admin"], ["mod"], ["user"]
    private Set<String> role;
}
