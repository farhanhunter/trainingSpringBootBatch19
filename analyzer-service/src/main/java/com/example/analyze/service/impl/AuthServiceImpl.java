package com.example.analyze.service.impl;

import com.example.analyze.dto.request.AuthRequestDto;
import com.example.analyze.dto.response.AuthResponseDto;
import com.example.analyze.entity.ERole;
import com.example.analyze.entity.Role;
import com.example.analyze.entity.UserEntity;
import com.example.analyze.exception.UnauthrorizedException;
import com.example.analyze.exception.UserAlreadyExistException;
import com.example.analyze.repository.RoleRepository;
import com.example.analyze.repository.UserRepository;
import com.example.analyze.service.AuthService;
import com.example.analyze.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public String register(AuthRequestDto authRequestDto) {
        if (userRepository.existsByUsername(authRequestDto.getUsername())) {
            throw new UserAlreadyExistException(authRequestDto.getUsername());
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(authRequestDto.getUsername());
        userEntity.setPassword(passwordEncoder.encode(authRequestDto.getPassword()));
        userEntity.setRoles(tentukanRoles(authRequestDto.getRole()));
        userRepository.save(userEntity);

        return "User " + userEntity.getUsername() + " has been registered successfully";
    }

    @Override
    public AuthResponseDto login(AuthRequestDto authRequestDto) {
        UserEntity user = userRepository.findByUsername(authRequestDto.getUsername());
        if (user == null || !passwordEncoder.matches(authRequestDto.getPassword(), user.getPassword())) {
            throw new UnauthrorizedException("Username atau Password Salah");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();
        return new AuthResponseDto(user.getUsername(), token, roles);
    }

    // sama seperti bezkoder: "admin" -> ADMIN, "mod" -> MODERATOR, lainnya / kosong -> USER
    private Set<Role> tentukanRoles(Set<String> namaRoles) {
        Set<Role> roles = new HashSet<>();
        if (namaRoles == null || namaRoles.isEmpty()) {
            roles.add(cariRole(ERole.ROLE_USER));
            return roles;
        }
        for (String nama : namaRoles) {
            switch (nama) {
                case "admin" -> roles.add(cariRole(ERole.ROLE_ADMIN));
                case "mod" -> roles.add(cariRole(ERole.ROLE_MODERATOR));
                default -> roles.add(cariRole(ERole.ROLE_USER));
            }
        }
        return roles;
    }

    private Role cariRole(ERole name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Role " + name + " belum ada di database"));
    }
}
