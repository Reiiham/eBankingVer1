package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.*;
import ma.ensa.ebankingver1.repository.AccountOperationRepository;
import ma.ensa.ebankingver1.repository.BankAccountRepository;
import ma.ensa.ebankingver1.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ma.ensa.ebankingver1.model.CurrentAccount;
import ma.ensa.ebankingver1.model.SavingAccount;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BankAccountServiceImpl implements BankAccountService {
    @Autowired
    private UserRepository ClientRepository;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private AccountOperationRepository accountOperationRepository;
    @Autowired
    private BankAccountMapperImpl dtoMapper;

    private static final Logger log = LoggerFactory.getLogger(BankAccountServiceImpl.class);
    @Override
    public ClientDTO saveClient(ClientDTO ClientDTO) {
        log.info("Saving new Client");
        User Client = dtoMapper.fromClientDTO(ClientDTO);
        User cust = ClientRepository.save(Client);
        return dtoMapper.fromClient(cust);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double InitialBalance, Long clientId, double overDraft) {
        User client = ClientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        if(client == null)
            throw new RuntimeException("Client not Found");
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreateAt(new Date());
        currentAccount.setBalance(InitialBalance);
        currentAccount.setClient(client);
        currentAccount.setOverDraft(overDraft);
        CurrentAccount SavedBankAccount = bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentBankAccount(SavedBankAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double InitialBalance, Long ClientId, double interestRate) {
        User Client = ClientRepository.findById(ClientId).orElse(null);
        if(Client == null)
            throw new RuntimeException("Client not Found");
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreateAt(new Date());
        savingAccount.setBalance(InitialBalance);
        savingAccount.setClient(Client);
        savingAccount.setInterestRate(interestRate);
        SavingAccount SavedBankAccount = bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(SavedBankAccount);
    }

    @Override
    public List<ClientDTO> listClients() {
        List<User> Clients = ClientRepository.findAll();
        List<ClientDTO> collect = Clients.stream().map(Client -> dtoMapper.fromClient(Client)).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<BankAccountDTO> bankAccountsList() {
        log.info("Fetching all bank accounts");
        List<BankAccount> ba = bankAccountRepository.findAll();
        List<BankAccountDTO> bankAccountDTOS = ba.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                return (BankAccountDTO) dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
            } else {
                return (BankAccountDTO) dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
            }
        }).collect(Collectors.toList());
        return bankAccountDTOS;
    }

    @Override
    public void debit(String accountId, double amount, String description) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("BankAccount not Found"));
        if(bankAccount.getBalance() < amount) throw new RuntimeException("Balance not sufficient");
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setOperationDate(new Date());
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("BankAccount not Found"));
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setOperationDate(new Date());
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfert(String accountIdSource, String accountIdDestination, double amount) {
        debit(accountIdSource,amount,"Transfert to " + accountIdDestination);
        credit(accountIdDestination,amount,"Transfert from " + accountIdSource);
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) {
        log.info("Fetching bank account with ID: {}", accountId);
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("BankAccount not Found"));
        if (bankAccount instanceof SavingAccount) {
            SavingAccount savingAccount = (SavingAccount) bankAccount;
            return dtoMapper.fromSavingBankAccount(savingAccount);
        } else {
            CurrentAccount currentAccount = (CurrentAccount) bankAccount;
            return dtoMapper.fromCurrentBankAccount(currentAccount);
        }
    }

    @Override
    public ClientDTO getClient(Long ClientId) throws Exception {
        User Client = ClientRepository.findById(ClientId).orElseThrow(()->new Exception("Client not found"));
        return dtoMapper.fromClient(Client);
    }

    @Override
    public ClientDTO updateClient(ClientDTO ClientDTO) {
        log.info("updating new Client");
        User Client = dtoMapper.fromClientDTO(ClientDTO);
        User cust = ClientRepository.save(Client);
        return dtoMapper.fromClient(cust);
    }

    @Override
    public void deleteClient(Long ClientId){
        ClientRepository.deleteById(ClientId);
    }

    @Override
    public List<AccountOperationDTO> AccountHistory(String accountId){
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId);
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.stream().map(accountOperation ->
                dtoMapper.fromAccountOperation(accountOperation)).collect(Collectors.toList());
        return accountOperationDTOS;
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElse(null);
        Page<AccountOperation> byBankAccountId = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page,size));
        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOS = byBankAccountId.getContent().stream().map(op->dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
        accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(byBankAccountId.getTotalPages());
        return accountHistoryDTO;
    }

    @Override
    public List<ClientDTO> searchClients(String keyword) {
        List<User> Clients = ClientRepository.findByLastNameContains(keyword);
        return Clients.stream().map(Client -> dtoMapper.fromClient(Client)).collect(Collectors.toList());
    }
    @Override
    public List<BankAccount> getaccountsClient(Long ClientId) {
        User Client = ClientRepository.findById(ClientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return bankAccountRepository.findByClient(Client);
    }
    @Override
    public List<BankAccountDTO> getAccountsByClientId(Long clientId) {
        List<BankAccount> accounts = bankAccountRepository.findByClientId(clientId);
        return accounts.stream()
                .map(account -> {
                    if (account instanceof CurrentAccount) {
                        return dtoMapper.fromCurrentBankAccount((CurrentAccount) account);
                    } else {
                        return dtoMapper.fromSavingBankAccount((SavingAccount) account);
                    }
                })
                .toList();
    }
}