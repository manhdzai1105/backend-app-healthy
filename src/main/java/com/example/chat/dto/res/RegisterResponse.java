package com.example.chat.dto.res;

import com.example.chat.enums.Role;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private Long id;
    private String email;
    private String username;
    private Role role;
    private LocalDateTime createdAt;
}
