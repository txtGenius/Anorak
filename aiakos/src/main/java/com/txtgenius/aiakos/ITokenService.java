package com.txtgenius.aiakos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import okhttp3.Interceptor;
import okhttp3.Response;


public interface ITokenService<TOKEN> {

    boolean isTokenExpiredLocal(@NotNull TOKEN token);

    boolean isTokenExpired(@NotNull Response response);

    @Nullable
    TOKEN refreshToken(@NotNull TOKEN oldToken);

    @NotNull
    Response performRequestWithToken(@NotNull Interceptor.Chain chain, @NotNull TOKEN token);
}
