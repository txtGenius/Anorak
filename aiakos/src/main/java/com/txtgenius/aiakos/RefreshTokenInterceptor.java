package com.txtgenius.aiakos;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class RefreshTokenInterceptor<TOKEN> implements Interceptor {

    private static final Object TOKEN_REFRESH_LOCK = new Object();

    private ITokenKeeper<TOKEN>  tokenKeeper;
    private ITokenService<TOKEN> tokenService;

    public RefreshTokenInterceptor(ITokenKeeper<TOKEN> tokenKeeper, ITokenService<TOKEN> tokenService) {
        this.tokenKeeper = tokenKeeper;
        this.tokenService = tokenService;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        TOKEN oldToken = tokenKeeper.getToken();
        // If old token not exist, execute it as normal request.
        // We are token refresher, not token getter.
        if (oldToken == null) {
            return chain.proceed(chain.request());
        } else if (tokenService.isTokenExpiredLocal(oldToken)) {
            synchronized (TOKEN_REFRESH_LOCK) {
                TOKEN newToken = tokenService.refreshToken(oldToken);
                if (newToken == null) {
                    throw new TokenRefreshFailException();
                }
                tokenKeeper.setToken(newToken);
            }
        }

        Response response = tokenService.performRequestWithToken(chain, oldToken);
        if (tokenService.isTokenExpired(response)) {
            synchronized (TOKEN_REFRESH_LOCK) {
                // To avoid pending thread refresh token again
                oldToken = tokenKeeper.getToken();
                if (tokenService.isTokenExpiredLocal(oldToken)) {
                    TOKEN newToken = tokenService.refreshToken(oldToken);
                    if (newToken == null) {
                        throw new TokenRefreshFailException();
                    }
                    tokenKeeper.setToken(newToken);

                    response = tokenService.performRequestWithToken(chain, newToken);
                }
            }
        }

        return response;
    }
}
