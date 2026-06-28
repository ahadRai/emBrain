package study.embrain.auth_service.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry-ms:900000}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-token-expiry-days:7}")
    private int refreshTokenExpiryDays;
}
