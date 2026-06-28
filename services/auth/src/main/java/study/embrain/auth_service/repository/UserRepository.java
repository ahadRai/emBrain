package study.embrain.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.embrain.auth_service.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
