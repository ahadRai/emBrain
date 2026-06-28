package study.embrain.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private UUID id;
    private String name;
    private String bio;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
