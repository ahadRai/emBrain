package study.embrain.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import study.embrain.user_service.dto.EnrolmentRequest;
import study.embrain.user_service.dto.ProfileResponse;
import study.embrain.user_service.dto.UpdateProfileRequest;
import study.embrain.user_service.entity.Enrolment;
import study.embrain.user_service.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── Profile ───────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(userService.getOrCreateProfile(UUID.fromString(userId)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(UUID.fromString(userId), req));
    }

    // ── Enrolments ────────────────────────────────────────────────────────────

    @GetMapping("/enrolments")
    public ResponseEntity<List<Enrolment>> getEnrolments(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(userService.getEnrolments(UUID.fromString(userId)));
    }

    @PostMapping("/enrolments")
    public ResponseEntity<Enrolment> enrol(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody EnrolmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.enrol(UUID.fromString(userId), req));
    }

    @DeleteMapping("/enrolments/{subject}")
    public ResponseEntity<Void> withdraw(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String subject) {
        userService.withdraw(UUID.fromString(userId), subject);
        return ResponseEntity.noContent().build();
    }

    // ── Health ────────────────────────────────────────────────────────────────

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "user");
    }
}
