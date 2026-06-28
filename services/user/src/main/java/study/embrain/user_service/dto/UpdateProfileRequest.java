package study.embrain.user_service.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 100, message = "Name must be 100 characters or fewer")
    private String name;

    @Size(max = 500, message = "Bio must be 500 characters or fewer")
    private String bio;
}
