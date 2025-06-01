package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> getUserRoleByEmail(String email);
    List<User> findByLastNameContains(String keyword);
    Optional<User>  findByUsername(String username);
    Optional<User> getUserRoleByUsername (String username);
}