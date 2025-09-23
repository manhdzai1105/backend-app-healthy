package com.example.chat.security;

import com.example.chat.entity.Account;
import com.example.chat.entity.UserDetail;
import com.example.chat.enums.Role;
import com.example.chat.repository.AccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountRepository accountRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        Account account = accountRepository.findByEmail(email)
                .orElseGet(() -> {
                    Account acc = new Account();
                    acc.setEmail(email);
                    acc.setUsername(name);
                    acc.setRole(Role.USER);
                    return acc;
                });

        UserDetail userDetail = account.getUserDetail();
        if (userDetail == null) {
            userDetail = new UserDetail();
            userDetail.setAccount(account);
            account.setUserDetail(userDetail);
            userDetail.setAvatar_url(picture);
        }

        account = accountRepository.save(account);

        String accessToken = jwtTokenProvider.generateAccessToken(
                account.getId(),
                account.getUsername(),
                account.getRole().name()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                account.getId(),
                account.getUsername(),
                account.getRole().name()
        );

        String redirectUrl = String.format(
                "http://localhost:8080?accessToken=%s&refreshToken=%s",
                accessToken,
                refreshToken
        );


        response.sendRedirect(redirectUrl);
    }
}
