package com.example.chat.dto;

import com.example.chat.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String avatarUrl;

    public static UserDto from(Account account) {
        return new UserDto(
                account.getId(),
                account.getUsername(),
                account.getUserDetail() != null ? account.getUserDetail().getAvatar_url() : null
        );
    }
}
