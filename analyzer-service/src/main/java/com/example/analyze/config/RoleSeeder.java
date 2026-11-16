package com.example.analyze.config;

import com.example.analyze.entity.ERole;
import com.example.analyze.entity.Role;
import com.example.analyze.entity.UserEntity;
import com.example.analyze.repository.RoleRepository;
import com.example.analyze.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Jalan sekali saat aplikasi start:
 * 1. isi tabel mst_role kalau belum ada (di bezkoder ini harus INSERT manual)
 * 2. user lama yang belum punya role diberi ROLE_USER
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        for (ERole name : ERole.values()) {
            if (roleRepository.findByName(name).isEmpty()) {
                roleRepository.save(new Role(name));
                log.info("Role {} dibuat", name);
            }
        }

        Role roleUser = roleRepository.findByName(ERole.ROLE_USER).orElseThrow();
        List<UserEntity> tanpaRole = userRepository.findAll().stream()
                .filter(user -> user.getRoles().isEmpty())
                .toList();
        for (UserEntity user : tanpaRole) {
            user.getRoles().add(roleUser);
        }
        userRepository.saveAll(tanpaRole);
        if (!tanpaRole.isEmpty()) {
            log.info("{} user lama diberi ROLE_USER", tanpaRole.size());
        }
    }
}
