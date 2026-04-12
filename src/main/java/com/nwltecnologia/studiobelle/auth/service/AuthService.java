package com.nwltecnologia.studiobelle.auth.service;

import com.nwltecnologia.studiobelle.auth.dto.*;
import com.nwltecnologia.studiobelle.auth.entity.RefreshToken;
import com.nwltecnologia.studiobelle.auth.repository.RefreshTokenRepository;
import com.nwltecnologia.studiobelle.security.ApiSecurityException;
import com.nwltecnologia.studiobelle.security.JwtService;
import com.nwltecnologia.studiobelle.security.PasswordService;
import com.nwltecnologia.studiobelle.tenant.TenantContext;
import com.nwltecnologia.studiobelle.user.entity.User;
import com.nwltecnologia.studiobelle.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final long refreshTokenTtlSeconds;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordService passwordService,
                       JwtService jwtService,
                       @Value("${app.security.jwt.refresh-token-ttl-seconds:604800}") long refreshTokenTtlSeconds) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String tenantId = currentTenant();
        userRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new ApiSecurityException("E-mail já cadastrado");
        });

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordService.hashPassword(request.password()))
                .role(request.role())
                .tenantId(tenantId)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        return generateTokens(saved);
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiSecurityException("Credenciais inválidas"));

        if (!user.getTenantId().equals(currentTenant())) {
            throw new ApiSecurityException("Credenciais inválidas para tenant atual");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ApiSecurityException("Usuário inativo");
        }

        if (!passwordService.matches(request.password(), user.getPasswordHash())) {
            throw new ApiSecurityException("Credenciais inválidas");
        }

        refreshTokenRepository.deleteByUser(user);
        return generateTokens(user);
    }

    @Transactional
    public AuthResponse refresh(TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new ApiSecurityException("Refresh token inválido"));

        if (Boolean.TRUE.equals(refreshToken.getRevoked()) || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiSecurityException("Refresh token expirado ou revogado");
        }

        User user = refreshToken.getUser();
        if (!user.getTenantId().equals(currentTenant())) {
            throw new ApiSecurityException("Refresh token não pertence ao tenant atual");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return generateTokens(user);
    }

    private AuthResponse generateTokens(User user) {
        JwtService.TokenData accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getTenantId(),
                user.getRole()
        );

        Instant refreshExpires = Instant.now().plusSeconds(refreshTokenTtlSeconds);
        String refreshValue = UUID.randomUUID().toString() + "." + UUID.randomUUID();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshValue)
                .user(user)
                .expiresAt(refreshExpires)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken.value(),
                refreshValue,
                "Bearer",
                accessToken.expiresAt(),
                refreshExpires
        );
    }

    private String currentTenant() {
        String tenant = TenantContext.getTenant();
        if (tenant == null || tenant.isBlank()) {
            throw new ApiSecurityException("Tenant não informado");
        }
        return tenant;
    }
}
