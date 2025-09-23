package com.example.chat.service;

import com.example.chat.dto.res.FavoriteDoctorResponse;
import com.example.chat.entity.Account;
import com.example.chat.entity.FavoriteDoctor;
import com.example.chat.repository.AccountRepository;
import com.example.chat.repository.DoctorReviewRepository;
import com.example.chat.repository.FavoriteDoctorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteDoctorService {
    private final FavoriteDoctorRepository favoriteDoctorRepository;
    private final AccountRepository accountRepository;
    private final DoctorReviewRepository  doctorReviewRepository;

    private Account getCurrentUser() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Người dùng không tồn tại"));
    }

    @Transactional
    public void addFavorite(Long doctorId) {
        Account user = getCurrentUser();
        Account doctor = accountRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Bác sĩ không tồn tại"));

        if (favoriteDoctorRepository.existsByUserAndDoctor(user, doctor)) {
            return;
        }

        FavoriteDoctor fav = FavoriteDoctor.builder()
                .user(user)
                .doctor(doctor)
                .build();

        favoriteDoctorRepository.save(fav);
    }

    @Transactional
    public void removeFavorite(Long doctorId) {
        Account user = getCurrentUser();
        Account doctor = accountRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Bác sĩ không tồn tại"));

        favoriteDoctorRepository.findByUserAndDoctor(user, doctor)
                .ifPresent(favoriteDoctorRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<FavoriteDoctorResponse> getFavorites() {
        Account user = getCurrentUser();
        List<FavoriteDoctor> favorites = favoriteDoctorRepository.findAllByUser(user);

        return favorites.stream().map(fav -> {
            Account doctor = fav.getDoctor();

            // ✅ Tính toán reviews
            Long totalReviews = doctorReviewRepository.countByDoctorId(doctor.getId());
            Number avgObj = doctorReviewRepository.avgRatingByDoctorId(doctor.getId());
            Double avgRating = avgObj != null ? avgObj.doubleValue() : 0.0;

            FavoriteDoctorResponse dto = new FavoriteDoctorResponse();
            dto.setId(doctor.getId());
            dto.setDoctorName(doctor.getUsername());
            dto.setDoctorAvatarUrl(
                    doctor.getDoctorDetail() != null
                            ? doctor.getDoctorDetail().getAvatar_url()
                            : null
            );
            dto.setSpecialization(
                    doctor.getDoctorDetail() != null
                            ? doctor.getDoctorDetail().getSpecialization()
                            : null
            );
            dto.setTotalReviews(totalReviews);
            dto.setAvgRating(avgRating);

            return dto;
        }).collect(Collectors.toList());
    }

}
