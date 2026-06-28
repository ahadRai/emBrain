package study.embrain.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * The User Service trusts identity forwarded by the API Gateway as the
     * X-User-Id / X-User-Role headers. JWT validation happens at the gateway
     * (auth_request → Auth Service /validate). Spring Security here only
     * ensures stateless session handling and disables CSRF for REST.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll()
                        .anyRequest().permitAll()   // gateway enforces auth; service trusts X-User-Id
                )
                .build();
    }
}
