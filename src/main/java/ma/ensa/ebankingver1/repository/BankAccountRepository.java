package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.BankAccount;
import ma.ensa.ebankingver1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    List<BankAccount> findByClient(User client);
    List<BankAccount> findByClientId(Long clientId);
}
