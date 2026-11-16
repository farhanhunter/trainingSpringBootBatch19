package com.example.analyze.repository;

import com.example.analyze.entity.ERole;
import com.example.analyze.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
