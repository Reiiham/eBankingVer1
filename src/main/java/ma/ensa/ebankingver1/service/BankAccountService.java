package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.BankAccount;
import ma.ensa.ebankingver1.model.User;

import java.util.List;

public interface BankAccountService {

    // Existing methods (add your current interface methods here)
    BankAccount findById(String id);
    BankAccount findByRib(String rib);
    BankAccount save(BankAccount account);
    List<BankAccount> findByUserId(Long userId);

    // Add these new methods for crypto trading
    boolean debitUser(User user, double amount, String reason);
    boolean creditUser(User user, double amount, String reason);

    // Add other existing methods from your current interface
    // ClientDTO saveClient(ClientDTO clientDTO);
    // CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, Long clientId, double overDraft);
    // SavingBankAccountDTO saveSavingBankAccount(double initialBalance, Long clientId, double interestRate);
    // List<ClientDTO> listClients();
    // List<BankAccountDTO> bankAccountsList();
    // void debit(String accountId, double amount, String description);
    // void credit(String accountId, double amount, String description);
    // void transfer(String accountSource, String accountDestination, double amount);
    // BankAccountDTO getBankAccount(String accountId);
    // ClientDTO getClient(Long clientId) throws Exception;
    // ClientDTO updateClient(ClientDTO clientDTO);
    // void deleteClient(Long clientId);
    // List<AccountOperationDTO> accountHistory(String accountId);
    // AccountHistoryDTO getAccountHistory(String accountId, int page, int size);
    // List<ClientDTO> searchClients(String keyword);
    // List<BankAccount> getaccountsClient(Long clientId);
    // List<BankAccountDTO> getAccountsByClientId(Long clientId);
    // List<BeneficiaryDTO> getBeneficiaries(String clientId);
    // BeneficiaryDTO addBeneficiary(String clientId, BeneficiaryDTO beneficiaryDTO);
    // List<BankAccountDTO> getAllBankAccounts();
}