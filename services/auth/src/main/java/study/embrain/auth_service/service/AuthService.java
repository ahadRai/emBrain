package study.embrain.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import study.embrain.auth_service.config.JwtConfig;
import study.embrain.auth_service.dto.AuthResponse;
import study.embrain.auth_service.dto.LoginRequest;
import study.embrain.auth_service.dto.RegisterRequest;
import study.embrain.auth_service.entity.RefreshToken;
import study.embrain.auth_service.entity.User;
import study.embrain.auth_service.repository.RefreshTokenRepository;
import study.embrain.auth_service.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redis;

    @Transactional
    public void register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .build();
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String rawToken) {
        String hash = hashToken(rawToken);

        // Check Redis blocklist first (fast path)
        if (Boolean.TRUE.equals(redis.hasKey("blocklist:" + hash))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token revoked");
        }

        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        // Revoke old token
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        redis.opsForValue().set("blocklist:" + hash, "1",
                Duration.ofDays(jwtConfig.getRefreshTokenExpiryDays()));

        return buildAuthResponse(stored.getUser());
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null) return;
        String hash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
        redis.opsForValue().set("blocklist:" + hash, "1",
                Duration.ofDays(jwtConfig.getRefreshTokenExpiryDays()));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = UUID.randomUUID().toString();
        String refreshHash = hashToken(rawRefreshToken);

        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshHash)
                .expiresAt(OffsetDateTime.now().plusDays(jwtConfig.getRefreshTokenExpiryDays()))
                .build();
        refreshTokenRepository.save(rt);

        // Store raw token in thread-local so controller can set it as cookie
        RefreshTokenHolder.set(rawRefreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .role(user.getRole())
                .build();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    // Simple thread-local to pass raw refresh token to controller without coupling
    public static class RefreshTokenHolder {
        private static final ThreadLocal<String> holder = new ThreadLocal<>();
        public static void set(String token) { holder.set(token); }
        public static String get() { return holder.get(); }
        public static void clear() { holder.remove(); }
    }
}
