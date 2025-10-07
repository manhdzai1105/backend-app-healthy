package com.example.chat.service;

import com.example.chat.dto.PagingResponse;
import com.example.chat.dto.req.UpdateAccountRequest;
import com.example.chat.dto.res.AccountResponse;
import com.example.chat.dto.res.DoctorResponse;
import com.example.chat.dto.res.ProfileUserResponse;
import com.example.chat.dto.res.UploadImageResponse;
import com.example.chat.entity.Account;
import com.example.chat.entity.UserDetail;
import com.example.chat.enums.Role;
import com.example.chat.mapper.AccountMapper;
import com.example.chat.repository.AccountRepository;
import com.example.chat.repository.UserDetailRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserDetailRepository userDetailRepository;

    private final AccountMapper accountMapper;
    private final CloudinaryService cloudinaryService;

    @Transactional(rollbackFor = Exception.class)
    public AccountResponse updateAccount(UpdateAccountRequest request) {

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 1. Tìm tài khoản
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Tài khoản không tồn tại"));

        // 2. Cập nhật dữ liệu account
        accountMapper.updateAccountFromDTO(request, account);

        // 3. Lấy hoặc tạo mới UserDetail
        UserDetail userDetail = account.getUserDetail();
        if (userDetail == null) {
            userDetail = new UserDetail();
            userDetail.setAccount(account); // liên kết 2 chiều
            account.setUserDetail(userDetail);
        }

        // 4. Cập nhật dữ liệu userDetail
        accountMapper.updateUserDetailFromDTO(request, userDetail);

        // 5. Lưu lại
        userDetailRepository.save(userDetail);
        accountRepository.save(account);

        // 6. Trả về DTO
        return accountMapper.toDto(account);
    }

    public PagingResponse<AccountResponse> getPagedAccounts(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Account> accountPage = accountRepository.findAll(pageable);

        List<AccountResponse> data = accountPage
                .stream()
                .map(accountMapper::toDto)
                .toList();

        return PagingResponse.<AccountResponse>builder()
                .code(200)
                .message("Lấy danh sách người dùng thành công")
                .page(accountPage.getNumber())
                .size(accountPage.getSize())
                .totalElements(accountPage.getTotalElements())
                .totalPages(accountPage.getTotalPages())
                .data(data)
                .build();
    }

    public PagingResponse<DoctorResponse> getPagedDoctors(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // 🔹 Giả sử Account có field role (Enum Role.DOCTOR)
        Page<Account> doctorPage = accountRepository.findAllByRole(Role.DOCTOR, pageable);

        List<DoctorResponse> data = doctorPage
                .stream()
                .map(accountMapper::toDoctorDto)
                .toList();

        return PagingResponse.<DoctorResponse>builder()
                .code(200)
                .message("Lấy danh sách bác sĩ thành công")
                .page(doctorPage.getNumber())
                .size(doctorPage.getSize())
                .totalElements(doctorPage.getTotalElements())
                .totalPages(doctorPage.getTotalPages())
                .data(data)
                .build();
    }


    @Transactional(readOnly = true)
    public ProfileUserResponse getProfile() {

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Người dùng không tồn tại"));

        return ProfileUserResponse.builder()
                .id(account.getId())  // 👈 set id
                .username(account.getUsername())
                .phone(account.getUserDetail() != null ? account.getUserDetail().getPhone() : null)
                .email(account.getEmail())
                .dateOfBirth(account.getUserDetail() != null ? account.getUserDetail().getDate_of_birth() : null)
                .gender(account.getUserDetail() != null ? account.getUserDetail().getGender().name() : null)
                .address(account.getUserDetail() != null ? account.getUserDetail().getAddress() : null)
                .build();
    }

    @Transactional
    public String updateAvatar(MultipartFile avatarFile) {
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException("Ảnh không được rỗng");
        }

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Người dùng không tồn tại"));

        // Upload ảnh lên Cloudinary
        UploadImageResponse uploadResponse = cloudinaryService.uploadFile(avatarFile);

        // Nếu chưa có UserDetail thì tạo mới
        UserDetail userDetail = account.getUserDetail();
        if (userDetail == null) {
            userDetail = new UserDetail();
            userDetail.setAccount(account);   // gắn quan hệ 1-1
            account.setUserDetail(userDetail);
        }

        // Cập nhật avatar
        userDetail.setAvatar_url(uploadResponse.getFileUrl());

        accountRepository.save(account);

        return uploadResponse.getFileUrl();
    }

}
