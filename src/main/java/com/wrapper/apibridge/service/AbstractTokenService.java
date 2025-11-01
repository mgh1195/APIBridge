package com.wrapper.apibridge.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter(value = AccessLevel.PROTECTED)
public abstract class AbstractTokenService {
    private String token;
    @Getter
    private String refreshToken;
    private Long expirationTime;

    public boolean isTokenExpired() {
        return System.currentTimeMillis() > expirationTime;
    }

    public String getToken() {
        if (isTokenExpired()) {
            refreshToken();
        }

        return token;
    }

    protected abstract void refreshToken();
}
