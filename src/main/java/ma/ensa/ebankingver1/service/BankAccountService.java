package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.BankAccount;

import java.util.List;

public interface BankAccountService {

    //Logger log = LoggerFactory.getLogger(this.getClass().getName());
    ClientDTO saveClient(ClientDTO ClientDTO);

    CurrentBankAccountDTO saveCurrentBankAccount(double InitialBalance, Long ClientId, double overDraft);
    SavingBankAccountDTO saveSavingBankAccount(double InitialBalance, Long ClientId, double interestRate);
    List<ClientDTO> listClients();
    BankAccountDTO getBankAccount(String accountId);
    void debit(String accountId,double amount,String description);
    void credit(String accountId,double amount,String description);
    void transfert(String accountIdSource,String accountIdDestination,double amount);
    List<BankAccountDTO> bankAccountsList();
    public ClientDTO getClient(Long ClientId) throws Exception;


    ClientDTO updateClient(ClientDTO ClientDTO);

    void deleteClient(Long ClientId);

    List<AccountOperationDTO> AccountHistory(String accountId);

    AccountHistoryDTO getAccountHistory(String accountId, int page, int size);

    List<ClientDTO> searchClients(String keyword);

    List<BankAccount> getaccountsClient(Long ClientId);
    List<BankAccountDTO> getAccountsByClientId(Long clientId);
}

