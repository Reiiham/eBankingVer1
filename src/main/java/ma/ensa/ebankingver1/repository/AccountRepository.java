package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
<<<<<<< HEAD
=======

>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
public interface AccountRepository extends JpaRepository<BankAccount, String> {
    Optional<BankAccount> findByUserId(Long userId);

    void deleteByUserId(Long userId);

//    @Query("SELECT a FROM BankAccount a WHERE LOWER(a.rawAccountNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
//    List<BankAccount> findByRawAccountNumberLike(@Param("query") String query);

}
