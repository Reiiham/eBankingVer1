package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.BankAccount;
import ma.ensa.ebankingver1.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface BankAccountService {

    // Méthodes existantes (déjà présentes)
    BankAccount findById(String id);
    BankAccount findByRib(String rib);
    BankAccount save(BankAccount account);
    List<BankAccount> findByUserId(Long userId);

    // Méthodes pour le crypto trading
    boolean debitUser(User user, double amount, String reason);
    boolean creditUser(User user, double amount, String reason);
    // Nouvelles méthodes avec RIB (numéro de compte)
    BigDecimal getAccountBalanceByRib(String rib);
    boolean validateTransactionPinByRib(String rib, String transactionPin);
    void debitAccountByRib(String rib, BigDecimal amount, String description);
    void creditAccountByRib(String rib, BigDecimal amount, String description);

}
