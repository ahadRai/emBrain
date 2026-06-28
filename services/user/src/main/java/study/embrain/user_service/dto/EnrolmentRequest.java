package study.embrain.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EnrolmentRequest {

    @NotBlank(message = "Subject is required")
    private String subject;
}
