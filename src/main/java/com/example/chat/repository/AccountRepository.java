package com.example.chat.repository;

import com.example.chat.entity.Account;
import com.example.chat.enums.AuthProvider;
import com.example.chat.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<Account> findByEmail(String email);
    List<Account> findByRole(Role role);

    Page<Account> findAllByRole(Role role, Pageable pageable);
}
