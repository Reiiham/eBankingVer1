package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.model.elasticsearch.CryptoTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CryptoTradeService {

    @Autowired
    private BinanceTestnetService binance;

    @Autowired
    private CryptoElasticSyncService elastic;

    @Autowired
    private BankAccountService bankService;

    @Autowired
    private UserService userService;

    @Autowired
    private CryptoRateService rateService;

    /**
     * Achète des cryptomonnaies
     */
    public String buyCrypto(Long userId, String symbol, double usdAmount) throws Exception {
        if (usdAmount <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        if (!symbol.endsWith("USDT")) {
            throw new IllegalArgumentException("Le symbole doit se terminer par USDT");
        }

        double currentPrice = rateService.getRate(symbol);
        BigDecimal qty = BigDecimal.valueOf(usdAmount).divide(BigDecimal.valueOf(currentPrice), 6, RoundingMode.DOWN);

        User user = userService.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }

        if (!bankService.debitUser(user, usdAmount, symbol)) {
            throw new IllegalArgumentException("Solde insuffisant");
        }

        try {
            String resp = binance.placeOrder(symbol, "BUY", "LIMIT", qty.doubleValue(), currentPrice);

            // Sauvegarde seulement si l'appel Binance réussit
            elastic.save(userId, symbol, "BUY", qty.doubleValue(), currentPrice);

            return resp;
        } catch (Exception e) {
            bankService.creditUser(user, usdAmount, "REFUND_" + symbol);
            throw new Exception("Erreur lors de l'achat: " + e.getMessage());
        }
    }

    /**
     * Vend des cryptomonnaies
     */
    public String sellCrypto(Long userId, String symbol, double qtyInput) throws Exception {
        if (qtyInput <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }

        BigDecimal qty = BigDecimal.valueOf(qtyInput).setScale(6, RoundingMode.DOWN);
        double currentPrice = rateService.getRate(symbol);
        double usdAmount = qty.doubleValue() * currentPrice;

        User user = userService.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }

        try {
            String resp = binance.placeOrder(symbol, "SELL", "LIMIT", qty.doubleValue(), currentPrice);

            bankService.creditUser(user, usdAmount, symbol);
            elastic.save(userId, symbol, "SELL", qty.doubleValue(), currentPrice);

            return resp;
        } catch (Exception e) {
            throw new Exception("Erreur lors de la vente: " + e.getMessage());
        }
    }

    /**
     * Récupère l'historique des transactions
     */
    public List<CryptoTransaction> getTransactionHistory(Long userId) {
        return elastic.findHistory(userId);
    }

    /**
     * Récupère le solde crypto (simplifié)
     */
    public Map<String, Double> getBalance(Long userId, String currency) {
        List<CryptoTransaction> transactions = elastic.findHistory(userId);

        // Filtrer si une crypto est spécifiée
        if (currency != null && !currency.isBlank()) {
            transactions = transactions.stream()
                    .filter(tx -> tx.getSymbol().startsWith(currency))
                    .toList();
        }

        Map<String, Double> balances = new HashMap<>();

        for (CryptoTransaction tx : transactions) {
            String symbol = tx.getSymbol();

            double qty = tx.getQuantity();
            if ("SELL".equalsIgnoreCase(tx.getSide())) {
                qty *= -1;
            }

            balances.put(symbol, balances.getOrDefault(symbol, 0.0) + qty);
        }

        // Arrondir les résultats à 6 décimales
        balances.replaceAll((k, v) -> Math.round(v * 1_000_000.0) / 1_000_000.0);

        return balances;
    }

    /**
     * Récupère les statistiques de trading
     */
    public Map<String, Object> getTradingStats(Long userId) {
        List<CryptoTransaction> transactions = elastic.findHistory(userId);

        long buyCount = transactions.stream().filter(t -> "BUY".equals(t.getSide())).count();
        long sellCount = transactions.stream().filter(t -> "SELL".equals(t.getSide())).count();

        double totalBuyValue = transactions.stream()
                .filter(t -> "BUY".equals(t.getSide()))
                .mapToDouble(t -> t.getQuantity() * t.getPrice()).sum();

        double totalSellValue = transactions.stream()
                .filter(t -> "SELL".equals(t.getSide()))
                .mapToDouble(t -> t.getQuantity() * t.getPrice()).sum();

        return Map.of(
                "totalTransactions", transactions.size(),
                "buyTransactions", buyCount,
                "sellTransactions", sellCount,
                "totalBuyValue", totalBuyValue,
                "totalSellValue", totalSellValue,
                "netValue", totalSellValue - totalBuyValue
        );
    }
    public double getPortfolioValue(Long userId) {
        List<CryptoTransaction> transactions = elastic.findHistory(userId);
        Map<String, Double> balances = new HashMap<>();

        // Calcul du solde par crypto
        for (CryptoTransaction tx : transactions) {
            String symbol = tx.getSymbol();
            double qty = tx.getQuantity();
            if ("SELL".equalsIgnoreCase(tx.getSide())) {
                qty *= -1;
            }
            balances.put(symbol, balances.getOrDefault(symbol, 0.0) + qty);
        }

        // Calcul de la valeur en USD
        double totalUsdValue = 0.0;
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            String symbol = entry.getKey();
            double qty = entry.getValue();
            if (qty <= 0) continue; // ignorer les cryptos vendues totalement

            try {
                double currentRate = rateService.getRate(symbol); // ex: BTCUSDT
                totalUsdValue += qty * currentRate;
            } catch (Exception e) {
                // ignorer les erreurs de rate individuel
            }
        }

        return Math.round(totalUsdValue * 100.0) / 100.0; // arrondi à 2 décimales
    }
    public List<Map<String, Object>> getCryptoStats(Long userId) {
        List<CryptoTransaction> txs = elastic.findHistory(userId);

        // Grouper par date et symbole
        return txs.stream().map(tx -> {
            Map<String, Object> map = new HashMap<>();
            map.put("symbol", tx.getSymbol());
            map.put("timestamp", tx.getTimestamp().toLocalDate().toString()); // Date sans heure
            map.put("side", tx.getSide());
            map.put("value", tx.getQuantity() * tx.getPrice());
            return map;
        }).collect(Collectors.toList());
    }

}

