package com.example.chat.dto.res;

import com.example.chat.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long id;
    private String email;
    private String username;
    private String avatar_url;
    private Role role;
    private String accessToken;
    private String refreshToken;
}
