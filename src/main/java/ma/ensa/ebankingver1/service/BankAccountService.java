package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.BankAccount;

import java.util.List;

public interface BankAccountService {

    BankAccount findById(String id);
    BankAccount findByRib(String rib);
    BankAccount save(BankAccount account);
    List<BankAccount> findByUserId(Long userId);
}

