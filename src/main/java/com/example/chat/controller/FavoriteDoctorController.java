package com.example.chat.controller;

import com.example.chat.dto.ApiResponse;
import com.example.chat.dto.res.FavoriteDoctorResponse;
import com.example.chat.service.FavoriteDoctorService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteDoctorController {
    private final FavoriteDoctorService favoriteDoctorService;

    @PostMapping("/{doctorId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> addFavorite(@PathVariable Long doctorId) {
        favoriteDoctorService.addFavorite(doctorId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Đã thêm bác sĩ vào danh sách yêu thích")
                        .build()
        );
    }

    @DeleteMapping("/{doctorId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable Long doctorId) {
        favoriteDoctorService.removeFavorite(doctorId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Đã xóa bác sĩ khỏi danh sách yêu thích")
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteDoctorResponse>>> getFavorites() {
        List<FavoriteDoctorResponse> favorites = favoriteDoctorService.getFavorites();
        return ResponseEntity.ok(
                ApiResponse.<List<FavoriteDoctorResponse>>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Danh sách bác sĩ yêu thích")
                        .data(favorites)
                        .build()
        );
    }

}
