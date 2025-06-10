package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.BankAccount;
import ma.ensa.ebankingver1.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface BankAccountService {

    BankAccount findById(String id);
    BankAccount findByRib(String rib);
    BankAccount save(BankAccount account);
    List<BankAccount> findByUserId(Long userId);
    List<BankAccount> findAll();
    String generateQRPaymentCode(QRPaymentRequest request);
    void processQRPayment(QRPaymentRequest request, Long userId);
    BankAccount testFindById(String id);

    // Add these new methods for crypto trading
    boolean debitUser(User user, double amount, String reason);
    boolean creditUser(User user, double amount, String reason);
    BigDecimal getAccountBalanceByRib(String rib);
    boolean validateTransactionPinByRib(String rib, String transactionPin);
    void debitAccountByRib(String rib, BigDecimal amount, String description);
    void creditAccountByRib(String rib, BigDecimal amount, String description);

    /*
    ClientDTO saveClient(ClientDTO clientDTO);

    CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, Long clientId, double overDraft);
    SavingBankAccountDTO saveSavingBankAccount(double initialBalance, Long clientId, double interestRate);
    List<ClientDTO> listClients();
    BankAccountDTO getBankAccount(String accountId);
    void debit(String accountId, double amount, String description);
    void credit(String accountId, double amount, String description);
    void transfer(String accountIdSource, String accountIdDestination, double amount); // Fixed typo
    List<BankAccountDTO> bankAccountsList();
    ClientDTO getClient(Long clientId) throws Exception;
    ClientDTO updateClient(ClientDTO clientDTO);
    void deleteClient(Long clientId);
    List<AccountOperationDTO> accountHistory(String accountId); // Fixed method name
    AccountHistoryDTO getAccountHistory(String accountId, int page, int size);
    List<ClientDTO> searchClients(String keyword);
    List<BankAccount> getaccountsClient(Long clientId);
    List<BankAccountDTO> getAccountsByClientId(Long clientId);
    List<BeneficiaryDTO> getBeneficiaries(String clientId); // Added
    BeneficiaryDTO addBeneficiary(String clientId, BeneficiaryDTO beneficiaryDTO);
    public List<BankAccountDTO> getAllBankAccounts();// Added

     */
}

