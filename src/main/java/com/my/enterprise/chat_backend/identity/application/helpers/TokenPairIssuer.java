package com.my.enterprise.chat_backend.identity.application.helpers;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.Clock;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.RefreshTokenRepository;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.TokenHasher;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.TokenService;
import com.my.enterprise.chat_backend.identity.domain.model.dto.AuthTokens;
import com.my.enterprise.chat_backend.identity.domain.model.aggregates.RefreshToken;
import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;

/**
 * Emisor compartido de pares de tokens (access + refresh).
 * <p>
 * Lo usan los command services que emiten sesiones
 * ({@code LoginService}, {@code RefreshSessionService}).
 * Genera el refresh token con entropía criptográfica (256 bits),
 * guarda solo su hash (regla de oro) y devuelve el token en texto
 * plano una única vez, dentro del DTO.
 */
public final class TokenPairIssuer {

    private final TokenService tokenService;
    private final TokenHasher tokenHasher;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public TokenPairIssuer(TokenService tokenService,
                           TokenHasher tokenHasher,
                           RefreshTokenRepository refreshTokenRepository,
                           Clock clock) {
        this.tokenService = tokenService;
        this.tokenHasher = tokenHasher;
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    public AuthTokens issue(User user) {
        TokenService.IssuedAccessToken accessToken = tokenService.issueAccessToken(user);

        String rawRefreshToken = Tokens.newRawRefreshToken();
        RefreshToken refreshToken = RefreshToken.issue(user.getId(), tokenHasher.hash(rawRefreshToken), clock.now());
        refreshTokenRepository.save(refreshToken);

        return new AuthTokens(
                accessToken.token(),
                rawRefreshToken,
                accessToken.expiresAt(),
                refreshToken.getExpiresAt());
    }
}
