package study.embrain.auth_service.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import study.embrain.auth_service.dto.AuthResponse;
import study.embrain.auth_service.dto.LoginRequest;
import study.embrain.auth_service.dto.RegisterRequest;
import study.embrain.auth_service.service.AuthService;
import study.embrain.auth_service.service.JwtService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    // ── POST /register ───────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration successful. Please log in."));
    }

    // ── POST /login ──────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                              HttpServletResponse response) {
        AuthResponse body = authService.login(req);
        setRefreshCookie(response);
        return ResponseEntity.ok(body);
    }

    // ── POST /refresh ────────────────────────────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(value = "refresh_token", required = false) String rawToken,
            HttpServletResponse response) {

        if (rawToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponse body = authService.refresh(rawToken);
        setRefreshCookie(response);
        return ResponseEntity.ok(body);
    }

    // ── POST /logout ─────────────────────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue(value = "refresh_token", required = false) String rawToken,
            HttpServletResponse response) {

        authService.logout(rawToken);
        clearRefreshCookie(response);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    // ── GET /validate — internal endpoint for Nginx auth_request ─────────────
    @GetMapping("/validate")
    public ResponseEntity<Void> validate(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                         HttpServletResponse response) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        if (!jwtService.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Forward user identity to downstream services via response headers
        response.setHeader("X-User-Id", jwtService.extractUserId(token));
        response.setHeader("X-User-Role", jwtService.extractRole(token));
        return ResponseEntity.ok().build();
    }

    // ── GET /health ──────────────────────────────────────────────────────────
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "auth");
    }

    // ── Cookie helpers ───────────────────────────────────────────────────────
    private void setRefreshCookie(HttpServletResponse response) {
        String raw = AuthService.RefreshTokenHolder.get();
        AuthService.RefreshTokenHolder.clear();
        if (raw == null) return;

        ResponseCookie cookie = ResponseCookie.from("refresh_token", raw)
                .httpOnly(true)
                .secure(false)               // set true behind HTTPS in prod
                .path("/api/v1/auth/refresh")
                .maxAge(7 * 24 * 60 * 60L)  // 7 days in seconds
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth/refresh")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
