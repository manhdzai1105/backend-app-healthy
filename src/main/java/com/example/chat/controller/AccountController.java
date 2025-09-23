package com.example.chat.controller;

import com.example.chat.dto.ApiResponse;
import com.example.chat.dto.PagingResponse;
import com.example.chat.dto.req.UpdateAccountRequest;
import com.example.chat.dto.res.AccountResponse;
import com.example.chat.dto.res.ProfileUserResponse;
import com.example.chat.entity.Account;
import com.example.chat.entity.UserDetail;
import com.example.chat.repository.AccountRepository;
import com.example.chat.service.AccountService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @PutMapping("/update-profile-user")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @RequestBody @Valid UpdateAccountRequest request
    ) {
        AccountResponse updated = accountService.updateAccount(request);

        return ResponseEntity.ok(
                ApiResponse.<AccountResponse>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Cập nhật tài khoản thành công")
                        .data(updated)
                        .build()
        );
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/users")
    public ResponseEntity<PagingResponse<AccountResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        // Gọi service để lấy PagingResponse
        PagingResponse<AccountResponse> pagingResponse = accountService.getPagedAccounts(page, size, sortBy, direction);

        return ResponseEntity.ok(pagingResponse);
    }

    @GetMapping("/profile-user")
    public ResponseEntity<ApiResponse<ProfileUserResponse>> getProfile() {
        ProfileUserResponse profile = accountService.getProfile();

        return ResponseEntity.ok(
                ApiResponse.<ProfileUserResponse>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Lấy thông tin người dùng thành công")
                        .data(profile)
                        .build()
        );
    }

    @PutMapping("/updated-avatar")
    public ResponseEntity<ApiResponse<String>> updateAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = accountService.updateAvatar(file);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(HttpServletResponse.SC_OK)
                .message("Cập nhật avatar thành công")
                .data(avatarUrl)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Tài khoản không tồn tại"));
        UserDetail userDetail = account.getUserDetail();

        return ResponseEntity.ok(Map.of(
                "userId", account.getId(),
                "username", account.getUsername(),
                "email", account.getEmail(),
                "role", account.getRole(),
                "avatar", userDetail != null ? userDetail.getAvatar_url() : "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBwgHBgkIBwgKCgkLDRYPDQwMDRsUFRAWIB0iIiAdHx8kKDQsJCYxJx8fLT0tMTU3Ojo6Iys/RD84QzQ5OjcBCgoKDQwNGg8PGjclHyU3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3N//AABEIAMAAzAMBIgACEQEDEQH/xAAbAAEAAgMBAQAAAAAAAAAAAAAAAQIEBQYDB//EADIQAQABAwICCAUDBQEAAAAAAAABAgMRBCEFMRIiMkFRYXGBExVSU5EzNEIjcqHB0RT/xAAWAQEBAQAAAAAAAAAAAAAAAAAAAQL/xAAWEQEBAQAAAAAAAAAAAAAAAAAAEQH/2gAMAwEAAhEDEQA/APogDTIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAmImZxEZBA9PgXpjMWq59kVW7lPaoqj1hBQEqIAQAAAAAAAAAAAAAAAADfujKMtnwjSdOr41e9MconxBbRcL6UU3NRnHdRDZ2rFq3GKKKY9nrEbJStIwiaYnnET7LCDC1HD7F6NqehV9VLS6rTXNNX0a4zE8qvF07x1NinUWqrdWN428lzRzAtctzarqoq5xO6rSACIAAAAAAAAAAAAAARHSqxHOXUaa1Fq1TREYiIc7o6OnqrdPjVDpk1cSAigAAANJxqzFN6m5THajf1a2G841TnSxV301NH6NYgAqACAAAAAAAAAAAAKyeG/vrXq6Ry2nq+Hft1d8S6imcxmO9NVICAAAADC4x+yq9Yc+3XHK8WaKPqqaVrEABAAAAAAAAAAAAAAD/AE6Dhmoi9pqfqp2mHPsjRamrTXYq/jO1UA6Uedq7RcoiqmqJjxejLQAAiTLXcT1kWqJtW5/qTz8ga/id/wCPqZiOxTtDFQNIACAAAAAAAAAAAAAAACqyNHq7mlq6u9M86W40/ErF2MTV0KvCpz5hNK6qLtFUZiuJ91LmqsW469ymPdzEZjkbzzSFbXV8U506eJ/ulq5mqqZmqc1TzmUClABAAABQAQAAAAAAATG847xUPazprt+Yi3RM+fc2Gh4ZExFzUc53ij/ra0UxTTEUxER4Qm6RqbXB8/q3falk08J00dqKqvWWfgSrGH8s0v25/MnyzS/bn8yzAGF8r0v0T+T5Xpfon8s0Bh/LNL9ufzKJ4XpZ/hMe7NAay5wezP6ddVM+bDv8Mv24macVx5N+jBSOUqpqpzExifCUOk1WjtaiOtTir6oaLV6a5pq5iuM091TVSPABQARAAAAAkJ5AjPe3XCtD0aYvXY608onuYHDdN/6L8TPYp3nzdDHkmriYSrnCcoqRGTIJEZMgkRkyCRGTIJEZMgl5XrNF2iaK4zE/4emUZBzer09WmuzRVy/jPi8HQ8S08X7E4jr07w57fPg1iAAgAAAATyExvMR4g3nCrXwtLFUx1qpzLNiXlajo2qI8KYhfKKsZVyZRVsmVcmQWyZVyZEWyZVyZBYVyZBYyrkFWyZVyZBOcue19v4WqrpjlnZ0GWo4zTiuivywuI1wtNuumnpVRtPKVVQAAAATT26fVC1Hbp9RXR9yVc5CCxlUyQWFdzcgtkV3MkFhXJkgsKhBbIqbkFjKu5uQWa7jMdKm1id8yz8y13F56tuPOSDAq+JMdaYmPDKmMeCPYEAAf/9k="
        ));
    }


}