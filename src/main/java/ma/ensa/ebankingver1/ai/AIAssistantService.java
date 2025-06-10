package ma.ensa.ebankingver1.ai;

import jakarta.persistence.Entity;
import ma.ensa.ebankingver1.model.*;
import ma.ensa.ebankingver1.repository.AccountRepository;
import ma.ensa.ebankingver1.repository.BeneficiaryRepository;
import ma.ensa.ebankingver1.repository.UserRepository;
import ma.ensa.ebankingver1.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIAssistantService {
    private static final Logger logger = LoggerFactory.getLogger(AIAssistantService.class);

    private final MyMemoryTranslateClient translator;
    @Autowired
    private WitAIClient witAIClient;

    @Autowired
    private ClientService clientService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private SuspensionService suspensionService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    public AIAssistantService() {
        this.translator = new MyMemoryTranslateClient();
    }

    public AIResponse processRequest(String input, String language, User user) {
        try {
            String inputEn = translateToEnglish(input, language);
            logger.info("Entrée traduite en anglais : {}", inputEn);

            String intent = extractIntent(inputEn);
            Map<String, String> params = extractParameters(inputEn);
            params = validateAndSuggest(params, user, intent);
            String message = handleIntent(intent, params, user, language);
            return new AIResponse(intent, message, !params.containsKey("error"), params);
        } catch (Exception e) {
            logger.error("Erreur lors du traitement : {}", e.getMessage());
            return new AIResponse("unknown", translateToTarget("Erreur : " + e.getMessage(), language), false, null);
        }
    }

    private String translateToEnglish(String input, String sourceLang) {
        if (sourceLang.equalsIgnoreCase("en")) return input;
        try {
            return translator.translate(input, sourceLang, "en");
        } catch (Exception e) {
            logger.error("Erreur de traduction vers l'anglais : {}", e.getMessage());
            return input;
        }
    }

    private String translateToTarget(String output, String targetLang) {
        if (targetLang.equalsIgnoreCase("en")) return output;
        try {
            return translator.translate(output, "en", targetLang);
        } catch (Exception e) {
            logger.error("Erreur de traduction vers la langue cible : {}", e.getMessage());
            return output;
        }
    }

    private String extractIntent(String input) {
        WitAIResponse witResponse = witAIClient.processRequest(input);
        return witResponse.getIntent() != null ? witResponse.getIntent() : "unknown";
    }

    private Map<String, String> extractParameters(String input) {
        WitAIResponse witResponse = witAIClient.processRequest(input);
        Map<String, Object> entities = witResponse.getEntities();
        Map<String, String> params = new HashMap<>();
        logger.info("Entités extraites par WitAI : {}", entities);
        if (entities != null) {
            entities.forEach((key, value) -> {
                if (value instanceof WitAIResponse.Entity[] && ((WitAIResponse.Entity[]) value).length > 0) {
                    Object entityValue = ((WitAIResponse.Entity[]) value)[0].getValue();
                    String entityName = key.split(":")[0]; // Extrait le nom avant le ":"
                    logger.info("Clé : {}, Valeur : {}", entityName, entityValue);
                    params.put(entityName, entityValue != null ? entityValue.toString() : "");
                }
            });
        }
        return params;
    }

    private Map<String, String> validateAndSuggest(Map<String, String> params, User user, String intent) {
        switch (intent) {
            case "make_transfer":
                if (!user.getServicesActifs().contains("VIREMENT")) {
                    params.put("error", "VIREMENT service not activated.");
                    return params;
                }
                if (!params.containsKey("amount")) {
                    params.put("error", "Missing amount. Please specify an amount, e.g., 'Transfer 100 MAD from I0a9QU1IIC1Eky049opaCQ== to 123456789012345678901234'.");
                    return params;
                }
                try {
                    Double.parseDouble(params.get("amount"));
                } catch (NumberFormatException e) {
                    params.put("error", "Invalid amount. Use a valid number (e.g., 100.50).");
                    return params;
                }
                if (!params.containsKey("account_id")) {
                    params.put("error", "Missing source account. Example: 'Transfer 100 MAD from I0a9QU1IIC1Eky049opaCQ== to 123456789012345678901234'.");
                    return params;
                }
                BankAccount sourceAccount = user.getAccounts().stream()
                        .filter(acc -> acc.getId().equals(params.get("account_id")))
                        .findFirst()
                        .orElse(null);
                if (sourceAccount == null) {
                    params.put("error", "Invalid or unauthorized source account.");
                    return params;
                }
                if (!params.containsKey("rib") && !params.containsKey("destination_account")) {
                    params.put("error", "Missing recipient RIB. Example: 'Transfer 100 MAD from I0a9QU1IIC1Eky049opaCQ== to 123456789012345678901234'.");
                    return params;
                }
                String recipientId = params.getOrDefault("rib", params.get("destination_account"));
                if (!recipientId.matches("\\d{24}")) {
                    params.put("error", "Invalid RIB. It must be exactly 24 digits (e.g., 123456789012345678901234).");
                    return params;
                }
                BankAccount destAccount = accountRepository.findById(recipientId)
                        .orElseGet(() -> accountRepository.findByRib(recipientId).orElse(null));
                if (destAccount == null) {
                    params.put("error", "Invalid recipient. Check the 24-digit RIB.");
                    return params;
                }
                params.put("sourceAccountId", sourceAccount.getId());
                params.put("destinationAccountId", destAccount.getId());
                break;
            case "recharge_phone":
                if (!params.containsKey("amount")) {
                    params.put("error", "Missing amount. Please specify an amount and phone number, e.g., 'Recharge 50 MAD on +212600000000'.");
                    return params;
                }
                try {
                    Double.parseDouble(params.get("amount"));
                } catch (NumberFormatException e) {
                    params.put("error", "Invalid amount. Use a valid number (e.g., 50.00).");
                    return params;
                }
                if (!params.containsKey("phone_number")) {
                    params.put("error", "Missing phone number. Example: 'Recharge 50 MAD on +212600000000'.");
                    return params;
                }
                break;
            case "add_beneficiary":
                if (!params.containsKey("rib") && !params.containsKey("destination_account")) {
                    params.put("error", "Missing RIB. Please specify a 24-digit RIB, e.g., 'Add 123456789012345678901234 as beneficiary'.");
                    return params;
                }
                String beneficiaryId = params.getOrDefault("rib", params.get("destination_account"));
                if (!beneficiaryId.matches("\\d{24}")) {
                    params.put("error", "Invalid RIB. It must be exactly 24 digits (e.g., 123456789012345678901234).");
                    return params;
                }
                BankAccount beneficiaryAccount = accountRepository.findById(beneficiaryId)
                        .orElseGet(() -> accountRepository.findByRib(beneficiaryId).orElse(null));
                if (beneficiaryAccount == null) {
                    params.put("error", "Invalid account or RIB.");
                    return params;
                }
                params.put("beneficiaryAccountId", beneficiaryAccount.getId());
                break;
            case "get_transactions":
                if (params.containsKey("time_range") && !params.get("time_range").matches("last\\s+(day|week|month)")) {
                    params.put("error", "Invalid time range. Example: 'Show transactions from last month'.");
                    return params;
                }
                break;
            case "check_balance":
                if (!user.getServicesActifs().contains("CONSULTATION_SOLDE")) {
                    params.put("error", "CONSULTATION_SOLDE service not activated.");
                    return params;
                }
                break;
            case "suspend_service":
                if (!user.getServicesActifs().contains("VIREMENT")) {
                    params.put("error", "VIREMENT service not activated, cannot suspend.");
                    return params;
                }
                break;
        }
        return params;
    }

    private String handleIntent(String intent, Map<String, String> params, User user, String language) {
        if (params.containsKey("error")) {
            return translateToTarget(params.get("error"), language);
        }
        switch (intent) {
            case "make_transfer":
                return handleTransfer(params, user, language);
            case "recharge_phone":
                return handleRechargePhone(params, user, language);
            case "check_balance":
                return handleCheckBalance(user, language);
            case "get_transactions":
                return handleGetTransactions(params, user, language);
                /*
            case "add_beneficiary":
                return handleAddBeneficiary(params, user, language);
            case "suspend_service":
                return handleSuspendService(user, language);

                 */
            default:
                return translateToTarget("I don't understand. Try 'Transfer 100 MAD from I0a9QU1IIC1Eky049opaCQ== to 123456789012345678901234' or 'Check my balance'.", language);
        }
    }

    private String handleTransfer(Map<String, String> params, User user, String language) {
        try {
            double amount = Double.parseDouble(params.get("amount"));
            String sourceAccountId = params.get("sourceAccountId");
            String destinationAccountId = params.get("destinationAccountId");

            BankAccount sourceAccount = accountRepository.findById(sourceAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("Source account not found."));
            BankAccount destAccount = accountRepository.findById(destinationAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("Destination account not found."));

            if (sourceAccount.getBalance() < amount) {
                return translateToTarget("Insufficient balance on source account.", language);
            }

            sourceAccount.setBalance(sourceAccount.getBalance() - amount);
            destAccount.setBalance(destAccount.getBalance() + amount);
            accountRepository.save(sourceAccount);
            accountRepository.save(destAccount);

            Transaction outgoing = new Transaction();
            outgoing.setUser(user);
            outgoing.setAccount(sourceAccount);
            outgoing.setType("VIREMENT_SORTANT");
            outgoing.setAmount(-amount);
            outgoing.setDate(LocalDateTime.now());
            transactionService.save(outgoing);

            Transaction incoming = new Transaction();
            incoming.setUser(destAccount.getUser());
            incoming.setAccount(destAccount);
            incoming.setType("VIREMENT_ENTRANT");
            incoming.setAmount(amount);
            incoming.setDate(LocalDateTime.now());
            transactionService.save(incoming);

            return translateToTarget("Transfer of " + amount + " MAD completed from " + sourceAccountId + " to " + destinationAccountId + ".", language);
        } catch (Exception e) {
            logger.error("Transfer error: {}", e.getMessage());
            return translateToTarget("Transfer failed: " + e.getMessage(), language);
        }
    }

    private String handleRechargePhone(Map<String, String> params, User user, String language) {
        try {
            double amount = Double.parseDouble(params.get("amount"));
            String phoneNumber = params.get("phone_number");
            logger.info("Simulating phone recharge of {} MAD to {}", amount, phoneNumber);
            return translateToTarget("Phone recharge of " + amount + " MAD to " + phoneNumber + " completed.", language);
        } catch (Exception e) {
            logger.error("Recharge error: {}", e.getMessage());
            return translateToTarget("Recharge failed: " + e.getMessage(), language);
        }
    }

    private String handleCheckBalance(User user, String language) {
        List<BankAccount> accounts = user.getAccounts();
        if (accounts.isEmpty()) {
            return translateToTarget("No bank accounts found.", language);
        }
        StringBuilder response = new StringBuilder(translateToTarget("Your balances:\n", language));
        for (BankAccount account : accounts) {
            response.append(translateToTarget("Account ", language))
                    .append(account.getId())
                    .append(": ")
                    .append(String.format("%.2f", account.getBalance()))
                    .append(" MAD\n");
        }
        return response.toString();
    }

    private String handleGetTransactions(Map<String, String> params, User user, String language) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusWeeks(1);
        if (params.containsKey("time_range")) {
            String timeRange = params.get("time_range");
            if (timeRange.contains("last day")) {
                startDate = endDate.minusDays(1);
            } else if (timeRange.contains("last month")) {
                startDate = endDate.minusMonths(1);
            }
        }
        List<Transaction> transactions = transactionService.findTransfersByUserId(user.getId(), 0, 10);
        if (transactions.isEmpty()) {
            return translateToTarget("No transactions found.", language);
        }
        StringBuilder response = new StringBuilder("Your recent transactions:\n");
        for (Transaction tx : transactions) {
            if (tx.getDate().isAfter(startDate) && tx.getDate().isBefore(endDate)) {
                response.append(tx.getType()).append(" of ").append(tx.getAmount())
                        .append(" MAD on ").append(tx.getDate()).append("\n");
            }
        }
        return translateToTarget(response.length() > "Your recent transactions:\n".length() ? response.toString() : "No transactions in this period.", language);
    }
/*
    private String handleAddBeneficiary(Map<String, String> params, User user, String language) {
        try {
            String accountId = params.get("beneficiaryAccountId");
            BankAccount destAccount = accountRepository.findById(accountId)
                    .orElse(null);
            if (destAccount == null) {
                return translateToTarget("Account not found.", language);
            }
            Beneficiary beneficiary = new Beneficiary();
            beneficiary.setRib(destAccount.getRib());
            beneficiary.setAccount(destAccount);
            beneficiary.setClient(user);
            beneficiaryRepository.save(beneficiary);
            return translateToTarget("Beneficiary " + accountId + " added.", language);
        } catch (Exception e) {
            logger.error("Error adding beneficiary: {}", e.getMessage());
            return translateToTarget("Failed to add beneficiary: " + e.getMessage(), language);
        }
    }



    private String handleSuspendService(User user, String language) {
        try {
            // Note: SuspensionService must implement suspendService(User user, BankService service)
            suspensionService.suspendService(user, BankService.VIREMENT);
            return translateToTarget("Virement service suspended.", language);
        } catch (Exception e) {
            logger.error("Suspend service error: {}", e.getMessage());
            return translateToTarget("Failed to suspend service: " + e.getMessage(), language);
        }
    }

 */
}