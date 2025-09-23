package com.example.chat.repository;

import com.example.chat.entity.Account;
import com.example.chat.entity.FavoriteDoctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FavoriteDoctorRepository extends JpaRepository<FavoriteDoctor, Long> {
    boolean existsByUserAndDoctor(Account user, Account doctor);
    Optional<FavoriteDoctor> findByUserAndDoctor(Account user, Account doctor);
    List<FavoriteDoctor> findAllByUser(Account user);

    @Query("""
           select fd.doctor.id
           from FavoriteDoctor fd
           where fd.user.id = :userId and fd.doctor.id in :doctorIds
           """)
    Set<Long> findFavoritedDoctorIds(Long userId, Collection<Long> doctorIds);
}
