package ma.ensa.ebankingver1.repository;

import ma.ensa.ebankingver1.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    long countBySuccessTrue();
}