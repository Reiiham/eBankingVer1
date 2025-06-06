package ma.ensa.ebankingver1.repository;
import ma.ensa.ebankingver1.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long clientId);
}
