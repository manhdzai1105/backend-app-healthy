package com.example.chat.repository;

import com.example.chat.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByAppTransId(String appTransId);
    Optional<Transaction> findByRefundId(String refundId);
}