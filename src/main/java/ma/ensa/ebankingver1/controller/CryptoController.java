package ma.ensa.ebankingver1.controller;

import ma.ensa.ebankingver1.model.elasticsearch.CryptoTransaction;
import ma.ensa.ebankingver1.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    @Autowired
    private CryptoTradeService tradeService;

    @Autowired
    private CryptoRateService rateService;

    @Autowired
    private BinanceTestnetService binanceTestnetService;

    @Autowired
    private CryptoElasticSyncService elasticService;

    @Autowired
    private UserService userService;

    // ✅ Achat de crypto (débit compte + Binance)
    @PostMapping("/buy")
    public ResponseEntity<?> buyCrypto(Authentication authentication,
                                       @RequestParam String symbol,
                                       @RequestParam double usdAmount) {
        try {
            Long userId = userService.getUserIdByUsername(authentication.getName());

            if (!symbol.endsWith("USDT")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le symbole doit se terminer par USDT (ex: BTCUSDT)"));
            }

            String result = tradeService.buyCrypto(userId, symbol, usdAmount);
            return ResponseEntity.ok(Map.of("message", "Achat réussi", "binanceResponse", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Vente de crypto (crédit compte + Binance)
    @PostMapping("/sell")
    public ResponseEntity<?> sellCrypto(Authentication authentication,
                                        @RequestParam String symbol,
                                        @RequestParam double quantity) {
        try {
            Long userId = userService.getUserIdByUsername(authentication.getName());
            String result = tradeService.sellCrypto(userId, symbol, quantity);
            return ResponseEntity.ok(Map.of("message", "Vente réussie", "binanceResponse", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/portfolio-value")
    public ResponseEntity<?> getPortfolioValue(Authentication auth) {
        try {
            Long userId = userService.getUserIdByUsername(auth.getName());
            double total = tradeService.getPortfolioValue(userId);
            return ResponseEntity.ok(Map.of("portfolioUsdValue", total));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getCryptoStats(Authentication auth) {
        try {
            Long userId = userService.getUserIdByUsername(auth.getName());
            List<Map<String, Object>> stats = tradeService.getCryptoStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    // ✅ Taux unique
    @GetMapping("/rate/{symbol}")
    public ResponseEntity<?> getRate(@PathVariable String symbol) {
        try {
            double rate = rateService.getRate(symbol);
            return ResponseEntity.ok(Map.of("symbol", symbol, "rate", rate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Tous les taux
    @GetMapping("/rates")
    public ResponseEntity<?> getAllRates() {
        try {
            Map<String, Double> rates = rateService.getAllRates();
            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Historique crypto depuis Elasticsearch
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        try {
            Long userId = userService.getUserIdByUsername(authentication.getName());
            List<CryptoTransaction> history = tradeService.getTransactionHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Recherche filtrée dans l'historique (via Elasticsearch)
    @GetMapping("/search")
    public ResponseEntity<?> searchTransactions(Authentication authentication,
                                                @RequestParam(required = false) String currency,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate) {
        try {
            Long userId = userService.getUserIdByUsername(authentication.getName());
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

            List<Map<String, Object>> results = elasticService.searchTransactions(userId, currency, start, end);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Solde local (peut être amélioré avec portefeuille réel)
    @GetMapping("/balance")
    public ResponseEntity<?> getCryptoBalance(Authentication authentication,
                                              @RequestParam(required = false) String currency) {
        try {
            Long userId = userService.getUserIdByUsername(authentication.getName());
            Map<String, Double> balances = tradeService.getBalance(userId, currency);
            return ResponseEntity.ok(balances);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Adresse de dépôt Binance Testnet
    @GetMapping("/testnet/address")
    public ResponseEntity<?> getTestnetAddress(@RequestParam String currency) {
        try {
            String address = binanceTestnetService.getDepositAddress(currency);
            return ResponseEntity.ok(Map.of("currency", currency, "address", address));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

