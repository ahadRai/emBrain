package study.embrain.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrolments", schema = "users",
        uniqueConstraints = @UniqueConstraint(name = "uq_enrolment", columnNames = {"user_id", "subject"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrolment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String subject;

    @Column(name = "enrolled_at")
    @Builder.Default
    private OffsetDateTime enrolledAt = OffsetDateTime.now();
}
