package com.example.Telegram_bot_for_Miro.auth;

import org.springframework.stereotype.Service;

@Service
public class TokenStorage {

    private String accessToken;
    private String refreshToken;
    private long expiresAt;

    public void save(String accessToken, String refreshToken, int expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = System.currentTimeMillis() + (expiresIn * 1000L);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}

