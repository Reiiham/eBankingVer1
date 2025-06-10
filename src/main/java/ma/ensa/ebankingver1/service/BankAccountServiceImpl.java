package ma.ensa.ebankingver1.service;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BankAccountServiceImpl implements BankAccountService {
    private static final Logger logger = LoggerFactory.getLogger(BankAccountServiceImpl.class);
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;
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
    @Override
    public List<BankAccount> findAll() {
        return bankAccountRepository.findAll();
    }
    private String toHex(String input) {
        if (input == null) return "null";
        StringBuilder hex = new StringBuilder();
        for (char c : input.toCharArray()) {
            hex.append(String.format("%02X", (int) c));
        }
        return hex.toString();
    }
    @Override
    public String generateQRPaymentCode(QRPaymentRequest request) {
        logger.info("Generating QR code for payment: RIB='{}', Amount={}", request.getRib(), request.getAmount());
        try {
            if (request.getRib() == null || request.getRib().isBlank()) {
                throw new IllegalArgumentException("RIB cannot be null or blank");
            }
            BankAccount destinationAccount = bankAccountRepository.findByRib(request.getRib());
            if (destinationAccount == null) {
                throw new EntityNotFoundException("No account found for RIB: " + request.getRib());
            }
            Map<String, Object> qrData = new HashMap<>();
            qrData.put("rib", request.getRib());
            qrData.put("amount", request.getAmount());
            qrData.put("description", request.getDescription());
            String qrContent = objectMapper.writeValueAsString(qrData);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(pngData);
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("rib", request.getRib());
            auditDetails.put("amount", String.valueOf(request.getAmount()));
            auditService.logAction("GENERATE_QR_PAYMENT", "QR_CODE", request.getRib(), auditDetails, true);
            return "data:image/png;base64," + base64Image;
        } catch (Exception e) {
            logger.error("Error generating QR code for RIB '{}': {}", request.getRib(), e.getMessage(), e);
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("rib", request.getRib());
            auditDetails.put("error", e.getMessage());
            auditService.logAction("GENERATE_QR_PAYMENT", "QR_CODE", request.getRib(), auditDetails, false);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    @Override
    @Transactional
    public void processQRPayment(QRPaymentRequest request, Long userId) {
        logger.info("Processing QR payment: SourceAccountId='{}', RIB='{}', Amount={}",
                request.getSourceAccountId(), request.getRib(), request.getAmount());
        try {
            if (request.getSourceAccountId() == null || request.getSourceAccountId().isBlank()) {
                throw new IllegalArgumentException("Invalid source account ID");
            }
            if (request.getRib() == null || request.getRib().isBlank()) {
                throw new IllegalArgumentException("Invalid RIB");
            }
            if (request.getAmount() <= 0) {
                throw new IllegalArgumentException("Invalid amount");
            }
            String sourceAccountId = request.getSourceAccountId().trim().toUpperCase();
            logger.debug("Normalized SourceAccountId: ID='{}', Length={}, Hex='{}'",
                    sourceAccountId, sourceAccountId.length(), toHex(sourceAccountId));
            BankAccount sourceAccount = bankAccountRepository.findById(sourceAccountId)
                    .orElseThrow(() -> {
                        logger.error("No source account found in database for ID: '{}'", sourceAccountId);
                        return new EntityNotFoundException("Invalid source account: " + sourceAccountId);
                    });
            logger.debug("Found source account: ID='{}', UserId='{}'",
                    sourceAccount.getId(), sourceAccount.getUser().getId());
            if (!sourceAccount.getUser().getId().equals(userId)) {
                logger.error("Source account '{}' does not belong to user: userId={}, accountUserId={}",
                        sourceAccountId, userId, sourceAccount.getUser().getId());
                throw new IllegalStateException("Invalid source account for user");
            }
            transfer(sourceAccountId, request.getRib(), request.getAmount());
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("sourceAccountId", sourceAccountId);
            auditDetails.put("rib", request.getRib());
            auditDetails.put("amount", String.valueOf(request.getAmount()));
            auditDetails.put("userId", String.valueOf(userId));
            auditService.logAction("PROCESS_QR_PAYMENT", "TRANSACTION", request.getRib(), auditDetails, true);
        } catch (Exception e) {
            logger.error("Transfer failed for SourceAccountId='{}', RIB='{}': {}",
                    request.getSourceAccountId(), request.getRib(), e.getMessage());
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("sourceAccountId", request.getSourceAccountId());
            auditDetails.put("rib", request.getRib());
            auditDetails.put("error", e.getMessage());
            auditDetails.put("userId", String.valueOf(userId));
            auditService.logAction("PROCESS_QR_PAYMENT", "TRANSACTION", request.getRib(), auditDetails, false);
            throw new RuntimeException("Failed to process QR payment: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void transfer(String sourceAccountId, String destinationRib, double amount) {
        logger.info("Starting transfer from: '{}' to RIB: '{}', amount: {}",
                sourceAccountId, destinationRib, amount);
        try {
            if (amount <= 0) {
                logger.warn("Transfer rejected: invalid amount '{}'", amount);
                throw new IllegalArgumentException("Invalid amount");
            }
            BankAccount sourceAccount = bankAccountRepository.findById(sourceAccountId)
                    .orElseThrow(() -> {
                        logger.error("No source account found: '{}'", sourceAccountId);
                        return new EntityNotFoundException("Source account not found: " + sourceAccountId);
                    });
            BankAccount destinationAccount = bankAccountRepository.findByRib(destinationRib);
            if (destinationAccount == null) {
                logger.debug("No direct account found for RIB: '{}', checking beneficiary", destinationRib);
                Optional<Beneficiary> beneficiaryOpt = beneficiaryRepository.findByRib(destinationRib);
                if (beneficiaryOpt.isPresent()) {
                    destinationAccount = beneficiaryOpt.get().getAccount();
                    if (destinationAccount == null) {
                        logger.error("No destination account linked to beneficiary for RIB: '{}'", destinationRib);
                        throw new EntityNotFoundException("No destination account found for RIB: " + destinationRib);
                    }
                    logger.debug("Found destination via beneficiary: ID '{}'", destinationAccount.getId());
                } else {
                    logger.error("No account or beneficiary found for RIB: '{}'", destinationRib);
                    throw new EntityNotFoundException("No account or beneficiary found for RIB: " + destinationRib);
                }
            }
            if (sourceAccount.getBalance() < amount) {
                logger.warn("Transfer rejected: insufficient balance in source '{}', balance: '{}'",
                        sourceAccountId, sourceAccount.getBalance());
                throw new RuntimeException("Insufficient balance in source account");
            }
            double newSourceBalance = sourceAccount.getBalance() - amount;
            double newDestinationBalance = destinationAccount.getBalance() + amount;
            sourceAccount.setBalance(newSourceBalance);
            destinationAccount.setBalance(newDestinationBalance);

            // Créer transaction débit
            Transaction debitTransaction = new Transaction();
            debitTransaction.setId(UUID.randomUUID().toString() + "_DEBIT");
            debitTransaction.setAccount(sourceAccount);
            debitTransaction.setAmount(-amount);
            debitTransaction.setType("VIREMENT_SORTANT");
            debitTransaction.setDate(LocalDateTime.now());
            debitTransaction.setUser(sourceAccount.getUser());
            debitTransaction.setCategory("QR_PAYMENT");

            // Créer transaction crédit
            Transaction creditTransaction = new Transaction();
            creditTransaction.setId(UUID.randomUUID().toString() + "_CREDIT");
            creditTransaction.setAccount(destinationAccount);
            creditTransaction.setAmount(amount);
            creditTransaction.setType("VIREMENT_ENTRANT");
            creditTransaction.setDate(LocalDateTime.now());
            creditTransaction.setUser(destinationAccount.getUser());
            creditTransaction.setCategory("QR_PAYMENT");

            // Sauvegarder les transactions
            transactionRepository.save(debitTransaction);
            transactionRepository.save(creditTransaction);

            // Ajouter à la liste des transactions du compte
            sourceAccount.getTransactions().add(debitTransaction);
            destinationAccount.getTransactions().add(creditTransaction);

            // Sauvegarder les comptes
            bankAccountRepository.save(sourceAccount);
            bankAccountRepository.save(destinationAccount);

            logger.info("Transfer completed: from '{}' (new balance: {}) to RIB '{}' (new balance: {})",
                    sourceAccountId, newSourceBalance, destinationRib, newDestinationBalance);
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("sourceAccountId", sourceAccountId);
            auditDetails.put("destinationRib", destinationRib);
            auditDetails.put("amount", String.valueOf(amount));
            auditService.logAction("TRANSFER", "TRANSACTION", destinationRib, auditDetails, true);
        } catch (Exception e) {
            logger.error("Transfer failed for source '{}', destination RIB '{}': {}",
                    sourceAccountId, destinationRib, e.getMessage());
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("sourceAccountId", sourceAccountId);
            auditDetails.put("destinationRib", destinationRib);
            auditDetails.put("error", e.getMessage());
            auditService.logAction("TRANSFER", "TRANSACTION", destinationRib, auditDetails, false);
            throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
        }
    }

    @Override
    public BankAccount testFindById(String id) {
        logger.info("Testing findById for ID: '{}', Hex: {}", id, toHex(id));
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test failed: account not found: " + id));
    }
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

    /*
    @Autowired
    private UserRepository clientRepository;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private AccountOperationRepository accountOperationRepository;
    @Autowired
    private BeneficiaryRepository beneficiaryRepository;
    @Autowired
    private BankAccountMapperImpl dtoMapper;

    private static final Logger log = LoggerFactory.getLogger(BankAccountServiceImpl.class);

    @Override
    public ClientDTO saveClient(ClientDTO clientDTO) {
        log.info("Saving new Client");
        User client = dtoMapper.fromClientDTO(clientDTO);
        User savedClient = clientRepository.save(client);
        return dtoMapper.fromClient(savedClient);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, Long clientId, double overDraft) {
        User client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreateAt(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setClient(client);
        currentAccount.setOverDraft(overDraft);
        CurrentAccount savedBankAccount = bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentBankAccount(savedBankAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, Long clientId, double interestRate) {
        User client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreateAt(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setClient(client);
        savingAccount.setInterestRate(interestRate);
        SavingAccount savedBankAccount = bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(savedBankAccount);
    }

    @Override
    public List<ClientDTO> listClients() {
        List<User> clients = clientRepository.findAll();
        return clients.stream().map(dtoMapper::fromClient).collect(Collectors.toList());
    }

    @Override
    public List<BankAccountDTO> bankAccountsList() {
        log.info("Fetching all bank accounts");
        List<BankAccount> accounts = bankAccountRepository.findAll();
        return accounts.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
            } else {
                return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public void debit(String accountId, double amount, String description) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("BankAccount not Found"));
        if (bankAccount.getBalance() < amount) throw new RuntimeException("Balance not sufficient");
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
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("BankAccount not Found"));
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
    @Transactional
    public void transfer(String accountSource, String accountDestination, double amount) {
        log.info("Initiating transfer: from {} to {}, amount: {}", accountSource, accountDestination, amount);

        if (amount <= 0) {
            log.warn("Transfer rejected: amount {} is not positive", amount);
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        BankAccount sourceAccount = bankAccountRepository.findById(accountSource)
                .orElseThrow(() -> {
                    log.error("Source account not found: {}", accountSource);
                    return new EntityNotFoundException("Source account not found: " + accountSource);
                });

        BankAccount destinationAccount;
        Optional<BankAccount> destinationById = bankAccountRepository.findById(accountDestination);
        if (destinationById.isPresent()) {
            destinationAccount = destinationById.get();
            log.debug("Destination found as direct account: {}", accountDestination);
        } else {
            Optional<Beneficiary> beneficiaryOpt = beneficiaryRepository.findByRib(accountDestination);
            if (beneficiaryOpt.isPresent()) {
                Beneficiary beneficiary = beneficiaryOpt.get();
                destinationAccount = beneficiary.getAccount();
                if (destinationAccount == null) {
                    log.error("Destination account not found for RIB: {}", accountDestination);
                    throw new EntityNotFoundException("Destination account not found for RIB: " + accountDestination);
                }
                log.debug("Destination resolved via beneficiary RIB: {}", accountDestination);
            } else {
                log.error("No account or beneficiary found for: {}", accountDestination);
                throw new EntityNotFoundException("No account or beneficiary found for: " + accountDestination);
            }
        }

        if (sourceAccount.getBalance() < amount) {
            log.warn("Transfer rejected: insufficient balance in source account {}, balance: {}", accountSource, sourceAccount.getBalance());
            throw new IllegalStateException("Insufficient balance in source account");
        }

        double newSourceBalance = sourceAccount.getBalance() - amount;
        double newDestinationBalance = destinationAccount.getBalance() + amount;
        sourceAccount.setBalance(newSourceBalance);
        destinationAccount.setBalance(newDestinationBalance);
        log.info("Transfer executed: from {} (new balance: {}) to {} (new balance: {})", accountSource, newSourceBalance, accountDestination, newDestinationBalance);

        bankAccountRepository.save(sourceAccount);
        bankAccountRepository.save(destinationAccount);

        AccountOperation debitOperation = new AccountOperation();
        debitOperation.setOperationDate(new Date());
        debitOperation.setType(OperationType.DEBIT);
        debitOperation.setAmount(amount);
        debitOperation.setDescription("Transfer to " + accountDestination);
        debitOperation.setBankAccount(sourceAccount);
        accountOperationRepository.save(debitOperation);

        AccountOperation creditOperation = new AccountOperation();
        creditOperation.setOperationDate(new Date());
        creditOperation.setType(OperationType.CREDIT);
        creditOperation.setAmount(amount);
        creditOperation.setDescription("Transfer from " + accountSource);
        creditOperation.setBankAccount(destinationAccount);
        accountOperationRepository.save(creditOperation);

        log.info("Transfer completed successfully: from {} to {}, amount: {}", accountSource, accountDestination, amount);
    }
    @Override
    public BankAccountDTO getBankAccount(String accountId) {
        log.info("Fetching bank account with ID: {}", accountId);
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("BankAccount not Found"));
        if (bankAccount instanceof SavingAccount) {
            return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
        } else {
            return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
        }
    }

    @Override
    public ClientDTO getClient(Long clientId) throws Exception {
        User client = clientRepository.findById(clientId)
                .orElseThrow(() -> new Exception("Client not found"));
        return dtoMapper.fromClient(client);
    }

    @Override
    public ClientDTO updateClient(ClientDTO clientDTO) {
        log.info("Updating client");
        User client = dtoMapper.fromClientDTO(clientDTO);
        User savedClient = clientRepository.save(client);
        return dtoMapper.fromClient(savedClient);
    }

    @Override
    public void deleteClient(Long clientId) {
        clientRepository.deleteById(clientId);
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId) {
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId);
        return accountOperations.stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElse(null);
        Page<AccountOperation> operations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        accountHistoryDTO.setAccountOperationDTOS(operations.getContent().stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList()));
        accountHistoryDTO.setAccountId(bankAccount != null ? bankAccount.getId() : "");
        accountHistoryDTO.setBalance(bankAccount != null ? bankAccount.getBalance() : 0.0);
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(operations.getTotalPages());
        return accountHistoryDTO;
    }

    @Override
    public List<ClientDTO> searchClients(String keyword) {
        List<User> clients = clientRepository.findByLastNameContains(keyword);
        return clients.stream().map(dtoMapper::fromClient).collect(Collectors.toList());
    }

    @Override
    public List<BankAccount> getaccountsClient(Long clientId) {
        User client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return bankAccountRepository.findByClient(client);
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

    @Override
    public List<BeneficiaryDTO> getBeneficiaries(String clientId) {
        Long id = Long.parseLong(clientId);
        List<Beneficiary> beneficiaries = beneficiaryRepository.findByClientId(id);
        return beneficiaries.stream()
                .map(beneficiary -> {
                    BeneficiaryDTO dto = new BeneficiaryDTO();
                    dto.setId(beneficiary.getId());
                    dto.setRib(beneficiary.getRib());
                    dto.setName(beneficiary.getName());
                    if (beneficiary.getAccount() != null) {
                        dto.setAccountId(beneficiary.getAccount().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Override
    public BeneficiaryDTO addBeneficiary(String clientId, BeneficiaryDTO beneficiaryDTO) {
        Long id = Long.parseLong(clientId);
        User client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(UUID.randomUUID().toString());
        beneficiary.setClient(client);
        beneficiary.setRib(beneficiaryDTO.getRib());
        beneficiary.setName(beneficiaryDTO.getName());

        // Vérifier et associer le compte bancaire
        if (beneficiaryDTO.getAccountId() == null || beneficiaryDTO.getAccountId().trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID is required for beneficiary");
        }
        BankAccount account = bankAccountRepository.findById(beneficiaryDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found: " + beneficiaryDTO.getAccountId()));
        beneficiary.setAccount(account);

        Beneficiary savedBeneficiary = beneficiaryRepository.save(beneficiary);

        BeneficiaryDTO result = new BeneficiaryDTO();
        result.setId(savedBeneficiary.getId());
        result.setRib(savedBeneficiary.getRib());
        result.setName(savedBeneficiary.getName());
        result.setAccountId(savedBeneficiary.getAccount() != null ? savedBeneficiary.getAccount().getId() : null);
        return result;
    }
    public List<BankAccountDTO> getAllBankAccounts() {
        List<BankAccount> accounts = bankAccountRepository.findAll();
        return accounts.stream()
                .map(account -> {
                    if (account instanceof SavingAccount) {
                        return dtoMapper.fromSavingBankAccount((SavingAccount) account);
                    } else {
                        return dtoMapper.fromCurrentBankAccount((CurrentAccount) account);
                    }
                })
                .collect(Collectors.toList());
    }

*/
}
