package ma.ensa.ebankingver1.repository;

import java.util.Optional;
import ma.ensa.ebankingver1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> getUserRoleByEmail(String email);
}
