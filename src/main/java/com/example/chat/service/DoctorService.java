package com.example.chat.service;

import com.example.chat.dto.req.CreateDoctorRequest;
import com.example.chat.dto.req.UpdateDoctorRequest;
import com.example.chat.dto.res.DoctorDetailResponse;
import com.example.chat.dto.res.DoctorResponse;
import com.example.chat.dto.res.DoctorListResponse;
import com.example.chat.dto.res.DoctorReviewResponse;
import com.example.chat.entity.Account;
import com.example.chat.entity.DoctorDetail;
import com.example.chat.entity.DoctorReview;
import com.example.chat.enums.Role;
import com.example.chat.exception.ConflictException;
import com.example.chat.mapper.AccountMapper;
import com.example.chat.repository.AccountRepository;
import com.example.chat.repository.DoctorDetailRepository;
import com.example.chat.repository.DoctorReviewRepository;
import com.example.chat.repository.FavoriteDoctorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final AccountRepository accountRepository;
    private final DoctorDetailRepository doctorDetailRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;
    private final DoctorReviewRepository doctorReviewRepository;
    private final FavoriteDoctorRepository  favoriteDoctorRepository;

    @Transactional(rollbackFor = Exception.class)
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email đã được sử dụng");
        }

        Account account = Account.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DOCTOR)
                .build();

        DoctorDetail detail = DoctorDetail.builder()
                .phone(request.getPhone())
                .gender(request.getGender())
                .date_of_birth(request.getDate_of_birth())
                .specialization(request.getSpecialization())
                .experienceYears(request.getExperience_years())
                .bio(request.getBio())
                .account(account)
                .build();

        account.setDoctorDetail(detail);
        return accountMapper.toDoctorDto(accountRepository.save(account));
    }

    @Transactional(rollbackFor = Exception.class)
    public DoctorResponse updateDoctorById(Long accountId, UpdateDoctorRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Tài khoản không tồn tại"));
        return updateAndSave(account, request);
    }

    @Transactional(rollbackFor = Exception.class)
    public DoctorResponse updateDoctor(UpdateDoctorRequest request) {
        return updateAndSave(getCurrentUser(), request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDoctorById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        accountRepository.delete(account);
    }

    public List<DoctorListResponse> getAllDoctors() {
        List<Account> doctors = accountRepository.findByRole(Role.DOCTOR);
        List<Long> doctorIds = doctors.stream().map(Account::getId).toList();
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Set<Long> favoritedIds = currentUserId != null && !doctorIds.isEmpty()
                ? favoriteDoctorRepository.findFavoritedDoctorIds(currentUserId, doctorIds)
                : Collections.emptySet();

        return doctors.stream().map(doctor -> {
            DoctorDetail detail = doctorDetailRepository.findByAccount_Id(doctor.getId()).orElse(null);

            Long totalReviews = doctorReviewRepository.countByDoctorId(doctor.getId());
            Number avgObj = doctorReviewRepository.avgRatingByDoctorId(doctor.getId());
            Double avgRating = avgObj != null ? avgObj.doubleValue() : 0;

            boolean isFav = favoritedIds.contains(doctor.getId());
            return buildDoctorDto(doctor, detail, totalReviews, avgRating, isFav);
        }).collect(Collectors.toList());
    }


    public List<DoctorListResponse> getTop3Doctors() {
        List<Object[]> result = doctorReviewRepository.findTopDoctorsByReviewCount();

        List<Long> doctorIds = result.stream().map(r -> (Long) r[0]).toList();
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Set<Long> favoritedIds = currentUserId != null && !doctorIds.isEmpty()
                ? favoriteDoctorRepository.findFavoritedDoctorIds(currentUserId, doctorIds)
                : Collections.emptySet();

        return result.stream()
                .limit(3)
                .map(obj -> {
                    Long doctorId = (Long) obj[0];
                    Long totalReviews = (Long) obj[1];
                    Number avgObj = (Number) obj[2];
                    Double avgRating = avgObj != null ? avgObj.doubleValue() : 0;

                    Account doctor = accountRepository.findById(doctorId).orElse(null);
                    DoctorDetail detail = doctorDetailRepository.findByAccount_Id(doctorId).orElse(null);

                    boolean isFav = favoritedIds.contains(doctorId);
                    return buildDoctorDto(doctor, detail, totalReviews, avgRating, isFav);
                })
                .collect(Collectors.toList());
    }

    public List<DoctorListResponse> getDoctorsBySpecialization(String specialization) {
        List<DoctorDetail> details = doctorDetailRepository.findBySpecializationIgnoreCase(specialization);

        List<Long> doctorIds = details.stream().map(d -> d.getAccount().getId()).toList();

        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Set<Long> favoritedIds = currentUserId != null && !doctorIds.isEmpty()
                ? favoriteDoctorRepository.findFavoritedDoctorIds(currentUserId, doctorIds)
                : Collections.emptySet();

        return details.stream().map(detail -> {
            Account doctor = detail.getAccount();

            Long totalReviews = doctorReviewRepository.countByDoctorId(doctor.getId());
            Number avgObj = doctorReviewRepository.avgRatingByDoctorId(doctor.getId());
            Double avgRating = avgObj != null ? avgObj.doubleValue() : 0;

            boolean isFav = favoritedIds.contains(doctor.getId());
            return buildDoctorDto(doctor, detail, totalReviews, avgRating, isFav);
        }).collect(Collectors.toList());
    }

    public DoctorDetailResponse getDoctorDetail(Long doctorId) {
        Account doctor = accountRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ"));

        DoctorDetail detail = doctorDetailRepository.findByAccount_Id(doctorId)
                .orElse(null);

        Long totalReviews = doctorReviewRepository.countByDoctorId(doctorId);
        Number avgObj = doctorReviewRepository.avgRatingByDoctorId(doctorId);
        Double avgRating = avgObj != null ? avgObj.doubleValue() : 0.0;

        // lấy danh sách review
        List<DoctorReview> reviews = doctorReviewRepository.findAllByDoctor_Id(doctorId);

        List<DoctorReviewResponse> reviewDtos = reviews.stream().map(r -> {
            DoctorReviewResponse dto = new DoctorReviewResponse();
            dto.setId(r.getId());
            dto.setUserId(r.getUser().getId());
            dto.setUsername(r.getUser().getUsername());
            dto.setUserAvatarUrl(
                    r.getUser().getUserDetail() != null
                            ? r.getUser().getUserDetail().getAvatar_url()
                            : null
            );
            dto.setRating(r.getRating());
            dto.setComment(r.getComment());
            dto.setCreatedAt(r.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());

        // build response
        DoctorDetailResponse dto = new DoctorDetailResponse();
        dto.setId(doctor.getId());
        dto.setDoctorName(doctor.getUsername());
        dto.setDoctorAvatarUrl(detail != null ? detail.getAvatar_url() : null);
        dto.setSpecialization(detail != null ? detail.getSpecialization() : null);
        dto.setExperienceYears(detail != null ? detail.getExperienceYears() : 0);
        dto.setBio(detail != null ? detail.getBio() : null);
        dto.setTotalReviews(totalReviews);
        dto.setAvgRating(avgRating);
        dto.setReviews(reviewDtos);

        return dto;
    }

    private DoctorListResponse buildDoctorDto(Account doctor, DoctorDetail detail,
                                              Long totalReviews, Double avgRating,
                                              boolean isFavorited) {
        DoctorListResponse dto = new DoctorListResponse();
        dto.setId(doctor != null ? doctor.getId() : null);
        dto.setDoctorName(doctor != null ? doctor.getUsername() : null);
        dto.setDoctorAvatarUrl(detail != null ? detail.getAvatar_url() : null);
        dto.setSpecialization(detail != null ? detail.getSpecialization() : null);
        dto.setExperienceYears(detail != null ? detail.getExperienceYears() : 0);
        dto.setTotalReviews(totalReviews != null ? totalReviews : 0);
        dto.setAvgRating(avgRating != null ? avgRating : 0);
        dto.setIsFavorited(isFavorited);
        return dto;
    }



    private DoctorResponse updateAndSave(Account account, UpdateDoctorRequest request) {
        validateEmailUnique(request.getEmail(), account.getEmail());
        accountMapper.updateDoctorFromDTO(request, account);

        DoctorDetail detail = account.getDoctorDetail();
        if (detail == null) {
            detail = new DoctorDetail();
            detail.setAccount(account);
            account.setDoctorDetail(detail);
        }

        accountMapper.updateDoctorDetailFromDTO(request, detail);
        doctorDetailRepository.save(detail);
        accountRepository.save(account);

        return accountMapper.toDoctorDto(account);
    }

    private Account getCurrentUser() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
    }

    private void validateEmailUnique(String newEmail, String currentEmail) {
        if (!newEmail.equals(currentEmail) && accountRepository.existsByEmail(newEmail)) {
            throw new ConflictException("Email đã được sử dụng");
        }
    }
}
