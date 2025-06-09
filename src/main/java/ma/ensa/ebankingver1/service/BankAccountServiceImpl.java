package ma.ensa.ebankingver1.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.*;
//import ma.ensa.ebankingver1.repository.AccountOperationRepository;
//import ma.ensa.ebankingver1.repository.BankAccountRepository;
import ma.ensa.ebankingver1.repository.BankAccountRepository;
import ma.ensa.ebankingver1.repository.BeneficiaryRepository;
import ma.ensa.ebankingver1.repository.TransactionRepository;
import ma.ensa.ebankingver1.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Override
    public BankAccount findById(String id) {
        return bankAccountRepository.findById(id).orElse(null);
    }

    @Override
    public BankAccount findByRib(String rib) {
        return bankAccountRepository.findByRib(rib);
    }

    @Override
    public BankAccount save(BankAccount account) {
        return bankAccountRepository.save(account);
    }

    @Override
    public List<BankAccount> findByUserId(Long userId) {
        return bankAccountRepository.findByUserId(userId);
    }


    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Débite le compte bancaire principal de l'utilisateur
     * @param user utilisateur à débiter
     * @param amount montant en USD
     * @param reason type de transaction (ex: BTCUSDT)
     * @return true si débit réussi, false sinon
     */

    @Transactional
    public boolean debitUser(User user, double amount, String reason) {
        // Récupération du premier compte bancaire (à adapter si multi-comptes)
        BankAccount account = bankAccountRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElse(null);

        if (account == null) {
            throw new IllegalStateException("Aucun compte bancaire trouvé pour l'utilisateur.");
        }

        if (account.getBalance() < amount) {
            return false;
        }

        // Mise à jour du solde
        account.setBalance(account.getBalance() - amount);
        bankAccountRepository.save(account);

        // Création de la transaction
        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setAccount(account);
        tx.setUser(user);
        tx.setAmount(amount);
        tx.setDate(LocalDateTime.now());
        tx.setType("ACHAT_CRYPTO_" + reason.toUpperCase());

        transactionRepository.save(tx);

        return true;
    }
    @Transactional
    public boolean creditUser(User user, double amount, String reason) {
        // Récupération du compte bancaire principal
        BankAccount account = bankAccountRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElse(null);

        if (account == null) {
            throw new IllegalStateException("Aucun compte bancaire trouvé pour l'utilisateur.");
        }

        // Crédit du solde
        account.setBalance(account.getBalance() + amount);
        bankAccountRepository.save(account);

        // Création de la transaction
        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setAccount(account);
        tx.setUser(user);
        tx.setAmount(amount);
        tx.setDate(LocalDateTime.now());
        tx.setType("CREDIT_CRYPTO_" + reason.toUpperCase());

        transactionRepository.save(tx);

        return true;
    }


        @Override
        public BigDecimal getAccountBalanceByRib(String rib) {
            BankAccount account = bankAccountRepository.findByRib(rib);
            if (account == null) {
                throw new EntityNotFoundException("Aucun compte bancaire trouvé pour le RIB: " + rib);
            }
            return BigDecimal.valueOf(account.getBalance());
        }

        @Override
        public boolean validateTransactionPinByRib(String rib, String transactionPin) {
            BankAccount account = bankAccountRepository.findByRib(rib);
            if (account == null) {
                throw new EntityNotFoundException("Aucun compte bancaire trouvé pour le RIB: " + rib);
            }
            return transactionPin != null && transactionPin.equals(account.getTransactionPin());        }

        @Override
        @Transactional
        public void debitAccountByRib(String rib, BigDecimal amount, String description) {
            BankAccount account = bankAccountRepository.findByRib(rib);
            if (account == null) {
                throw new EntityNotFoundException("Aucun compte bancaire trouvé pour le RIB: " + rib);
            }

            BigDecimal currentBalance = BigDecimal.valueOf(account.getBalance());
            if (currentBalance.compareTo(amount) < 0) {
                throw new IllegalStateException("Solde insuffisant pour le débit sur le compte RIB: " + rib);
            }

            // Update balance
            account.setBalance(currentBalance.subtract(amount).doubleValue());
            bankAccountRepository.save(account);

            // Create transaction record
            Transaction tx = new Transaction();
            tx.setId(UUID.randomUUID().toString());
            tx.setAccount(account);
            tx.setUser(account.getUser());
            tx.setAmount(amount.doubleValue());
            tx.setDate(LocalDateTime.now());
            tx.setType("DEBIT_" + description.toUpperCase());

            transactionRepository.save(tx);
        }

        @Override
        @Transactional
        public void creditAccountByRib(String rib, BigDecimal amount, String description) {
            BankAccount account = bankAccountRepository.findByRib(rib);
            if (account == null) {
                throw new EntityNotFoundException("Aucun compte bancaire trouvé pour le RIB: " + rib);
            }

            // Update balance
            BigDecimal currentBalance = BigDecimal.valueOf(account.getBalance());
            account.setBalance(currentBalance.add(amount).doubleValue());
            bankAccountRepository.save(account);

            // Create transaction record
            Transaction tx = new Transaction();
            tx.setId(UUID.randomUUID().toString());
            tx.setAccount(account);
            tx.setUser(account.getUser());
            tx.setAmount(amount.doubleValue());
            tx.setDate(LocalDateTime.now());
            tx.setType("CREDIT_" + description.toUpperCase());

            transactionRepository.save(tx);
        }
    }

