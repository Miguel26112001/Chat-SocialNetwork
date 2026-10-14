package com.my.enterprise.chat_backend.identity.infrastructure.adapters;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.TokenService;
import com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidCredentialsException;
import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.UserRole;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Adaptador del puerto {@code TokenService}: JWT firmados con HS256
 * (Nimbus JOSE JWT, ya presente vía spring-boot-starter-oauth2-resource-server).
 * <p>
 * Configuración (application.yaml):
 *  - security.jwt.secret: clave simétrica (mínimo 256 bits);
 *  - security.jwt.access-token-ttl: duración del access token.
 * <p>
 * El access token es de corta duración; la seguridad a largo plazo la
 * aporta el refresh token (rotación + revocación en BD).
 */
@Component
public class JwtTokenService implements TokenService {

    private final SecretKey key;
    private final Duration accessTokenTtl;

    public JwtTokenService(@Value("${security.jwt.secret}") String secret,
                           @Value("${security.jwt.access-token-ttl:15m}") Duration accessTokenTtl) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "security.jwt.secret debe tener al menos 32 bytes (256 bits) para HS256");
        }
        this.key = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.accessTokenTtl = accessTokenTtl;
    }

    @Override
    public IssuedAccessToken issueAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessTokenTtl);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiresAt))
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try {
            JWSSigner signer = new MACSigner(key);
            jwt.sign(signer);
        } catch (JOSEException e) {
            throw new IllegalStateException("No se pudo firmar el access token", e);
        }
        return new IssuedAccessToken(jwt.serialize(), expiresAt);
    }

    @Override
    public AccessTokenClaims parseClaims(String accessToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(accessToken);
            if (!jwt.verify(new MACVerifier(key))) {
                throw new InvalidCredentialsException();
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.before(Date.from(Instant.now()))) {
                throw new InvalidCredentialsException();
            }
            UUID userId = UUID.fromString(claims.getSubject());
            UserRole role = UserRole.valueOf(claims.getStringClaim("role"));
            return new AccessTokenClaims(userId, role);
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            // Cualquier problema de formato, firma o claims se traduce
            // a la misma excepción: no revelar el motivo exacto.
            throw new InvalidCredentialsException();
        }
    }
}
