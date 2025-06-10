package ma.ensa.ebankingver1.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import ma.ensa.ebankingver1.model.elasticsearch.CryptoTransaction;
import ma.ensa.ebankingver1.repository.elasticsearch.CryptoTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CryptoElasticSyncService {

    @Autowired
    private CryptoTransactionRepository repository;

    @Autowired(required = false) // Make optional to prevent startup issues
    private ElasticsearchClient elasticsearchClient;

    /**
     * Sauvegarde une transaction crypto (with error handling)
     */
    public void save(Long userId, String symbol, String side, double quantity, double price) {
        try {
            CryptoTransaction transaction = new CryptoTransaction();
            transaction.setId(userId + "_" + symbol + "_" + LocalDateTime.now());
            transaction.setUserId(userId);
            transaction.setSymbol(symbol);
            transaction.setSide(side);
            transaction.setQuantity(quantity);
            transaction.setPrice(price);
            transaction.setTimestamp(LocalDateTime.now());

            // Try to save with repository first (safer)
            CryptoTransaction saved = repository.save(transaction);
            System.out.println("Transaction saved successfully: " + saved.getId());

        } catch (Exception e) {
            // Log the error but don't fail the transaction
            System.err.println("Failed to save to Elasticsearch: " + e.getMessage());
            // You could also save to a backup database here
            saveToBackupStorage(userId, symbol, side, quantity, price);
        }
    }

    /**
     * Alternative save method using direct client (if needed)
     */
    public void saveWithClient(Long userId, String symbol, String side, double quantity, double price) {
        try {
            if (elasticsearchClient == null) {
                System.err.println("Elasticsearch client not available");
                return;
            }

            CryptoTransaction transaction = new CryptoTransaction();
            transaction.setId(userId + "_" + symbol + "_" + LocalDateTime.now());
            transaction.setUserId(userId);
            transaction.setSymbol(symbol);
            transaction.setSide(side);
            transaction.setQuantity(quantity);
            transaction.setPrice(price);
            transaction.setTimestamp(LocalDateTime.now());

            IndexResponse response = elasticsearchClient.index(builder -> builder
                    .index("crypto_transactions")
                    .id(transaction.getId().toString())
                    .document(transaction)
            );

            // Handle seqNo() safely
            try {
                Long seqNo = response.seqNo();
                System.out.println("Transaction indexed with seqNo: " + seqNo);
            } catch (NoSuchMethodError e) {
                // seqNo() method not available, use alternative
                System.out.println("Transaction indexed successfully (seqNo not available)");
            }

        } catch (Exception e) {
            System.err.println("Failed to save with client: " + e.getMessage());
            saveToBackupStorage(userId, symbol, side, quantity, price);
        }
    }

    /**
     * Backup storage method (could save to database)
     */
    private void saveToBackupStorage(Long userId, String symbol, String side, double quantity, double price) {
        // Implement backup storage logic here
        System.out.println("Saving to backup: User=" + userId + ", Symbol=" + symbol +
                ", Side=" + side + ", Qty=" + quantity + ", Price=" + price);
    }

    /**
     * Récupère l'historique des transactions
     */
    public List<CryptoTransaction> findHistory(Long userId) {
        try {
            return repository.findByUserIdOrderByTimestampDesc(userId);
        } catch (Exception e) {
            System.err.println("Failed to fetch history: " + e.getMessage());
            return List.of(); // Return empty list instead of failing
        }
    }

    /**
     * Recherche avec filtres (with error handling)
     */
    public List<Map<String, Object>> searchTransactions(Long userId, String currency,
                                                        LocalDateTime start, LocalDateTime end) {
        try {
            List<CryptoTransaction> transactions;

            if (currency != null && start != null && end != null) {
                transactions = repository.findByUserIdAndSymbolAndTimestampBetweenOrderByTimestampDesc(
                        userId, currency, start, end);
            } else if (start != null && end != null) {
                transactions = repository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
                        userId, start, end);
            } else {
                transactions = repository.findByUserIdOrderByTimestampDesc(userId);
            }

            // Convert to Map format
            return transactions.stream()
                    .map(this::transactionToMap)
                    .toList();

        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage());
            return List.of(); // Return empty list instead of failing
        }
    }

    private Map<String, Object> transactionToMap(CryptoTransaction transaction) {
        return Map.of(
                "id", transaction.getId(),
                "userId", transaction.getUserId(),
                "symbol", transaction.getSymbol(),
                "side", transaction.getSide(),
                "quantity", transaction.getQuantity(),
                "price", transaction.getPrice(),
                "timestamp", transaction.getTimestamp()
        );
    }
}