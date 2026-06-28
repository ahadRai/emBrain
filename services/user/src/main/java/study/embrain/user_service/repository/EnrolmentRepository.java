package study.embrain.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.embrain.user_service.entity.Enrolment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrolmentRepository extends JpaRepository<Enrolment, UUID> {
    List<Enrolment> findByUserId(UUID userId);
    Optional<Enrolment> findByUserIdAndSubject(UUID userId, String subject);
    boolean existsByUserIdAndSubject(UUID userId, String subject);
}
