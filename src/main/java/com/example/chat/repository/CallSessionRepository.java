package com.example.chat.repository;

import com.example.chat.entity.CallSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface CallSessionRepository extends JpaRepository<CallSession,Long> {
    @NonNull
    Optional<CallSession> findById(@NonNull Long id);

    @Query("SELECT cs.account.id FROM CallSession cs WHERE cs.id = :id")
    Long findCallerIdById(@Param("id") Long id);

}
