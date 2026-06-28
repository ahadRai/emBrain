package study.embrain.user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import study.embrain.user_service.dto.EnrolmentRequest;
import study.embrain.user_service.dto.ProfileResponse;
import study.embrain.user_service.dto.UpdateProfileRequest;
import study.embrain.user_service.entity.Enrolment;
import study.embrain.user_service.entity.Profile;
import study.embrain.user_service.repository.EnrolmentRepository;
import study.embrain.user_service.repository.ProfileRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ProfileRepository profileRepository;
    private final EnrolmentRepository enrolmentRepository;

    @Transactional
    public ProfileResponse getOrCreateProfile(UUID userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseGet(() -> profileRepository.save(
                        Profile.builder().id(userId).build()
                ));
        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        Profile profile = profileRepository.findById(userId)
                .orElseGet(() -> profileRepository.save(Profile.builder().id(userId).build()));

        if (req.getName() != null) profile.setName(req.getName());
        if (req.getBio() != null)  profile.setBio(req.getBio());
        profile.setUpdatedAt(OffsetDateTime.now());

        return toResponse(profileRepository.save(profile));
    }

    public List<Enrolment> getEnrolments(UUID userId) {
        return enrolmentRepository.findByUserId(userId);
    }

    @Transactional
    public Enrolment enrol(UUID userId, EnrolmentRequest req) {
        if (enrolmentRepository.existsByUserIdAndSubject(userId, req.getSubject())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled in " + req.getSubject());
        }
        return enrolmentRepository.save(
                Enrolment.builder().userId(userId).subject(req.getSubject()).build()
        );
    }

    @Transactional
    public void withdraw(UUID userId, String subject) {
        Enrolment e = enrolmentRepository.findByUserIdAndSubject(userId, subject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrolment not found"));
        enrolmentRepository.delete(e);
    }

    private ProfileResponse toResponse(Profile p) {
        return ProfileResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .bio(p.getBio())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
