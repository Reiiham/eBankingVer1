
package ma.ensa.ebankingver1.controller;

import jakarta.persistence.EntityNotFoundException;
import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.service.BankAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final BankAccountService bankAccountService;

    public ClientController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/{clientId}/accounts")
    public ResponseEntity<List<BankAccountDTO>> getClientAccounts(@PathVariable("clientId") String clientId) {
        Long id = parseClientId(clientId);
        List<BankAccountDTO> accounts = bankAccountService.getAccountsByClientId(id);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{clientId}/accounts/{accountId}")
    public ResponseEntity<BankAccountDTO> getClientAccount(@PathVariable("clientId") String clientId, @PathVariable("accountId") String accountId) {
        BankAccountDTO account = bankAccountService.getBankAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{clientId}/accounts/{accountId}/operations")
    public ResponseEntity<List<AccountOperationDTO>> getAccountOperations(@PathVariable("clientId") String clientId, @PathVariable("accountId") String accountId) {
        List<AccountOperationDTO> operations = bankAccountService.accountHistory(accountId);
        return ResponseEntity.ok(operations);
    }

    @GetMapping("/{clientId}/accounts/operations")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecentTransactions(
            @PathVariable("clientId") String clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            if (page < 0 || size <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, null, "Invalid pagination parameters: page must be >= 0, size must be > 0"));
            }

            Long id = parseClientId(clientId);
            List<BankAccountDTO> accounts = bankAccountService.getAccountsByClientId(id);
            if (accounts == null || accounts.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("transactions", List.of());
                response.put("total", 0);
                response.put("page", page);
                response.put("size", size);
                response.put("totalPages", 0);
                return ResponseEntity.ok(new ApiResponse<>(true, response, null));
            }

            List<AccountOperationDTO> allOperations = accounts.stream()
                    .flatMap(account -> {
                        List<AccountOperationDTO> history = bankAccountService.accountHistory(account.getId());
                        return history != null ? history.stream() : Stream.empty();
                    })
                    .filter(operation -> operation.getOperationDate() != null)
                    .sorted((o1, o2) -> o2.getOperationDate().compareTo(o1.getOperationDate()))
                    .collect(Collectors.toList());

            int total = allOperations.size();
            int start = Math.min(page * size, total);
            int end = Math.min(start + size, total);
            List<AccountOperationDTO> paginatedOperations = (start < end) ? allOperations.subList(start, end) : List.of();

            Map<String, Object> response = new HashMap<>();
            response.put("transactions", paginatedOperations);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) total / size));

            return ResponseEntity.ok(new ApiResponse<>(true, response, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to fetch transactions: " + e.getMessage()));
        }
    }

    private Long parseClientId(String clientId) {
        try {
            return Long.parseLong(clientId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid clientId: " + clientId);
        }
    }

    @PostMapping("/{clientId}/accounts/{accountId}/debit")
    public ResponseEntity<DebitDTO> debitAccount(@PathVariable("clientId") String clientId, @PathVariable("accountId") String accountId, @RequestBody DebitDTO debitDTO) {
        bankAccountService.debit(accountId, debitDTO.getAmount(), debitDTO.getDescription());
        return ResponseEntity.ok(debitDTO);
    }

    @PostMapping("/{clientId}/accounts/{accountId}/credit")
    public ResponseEntity<CreditDTO> creditAccount(@PathVariable("clientId") String clientId, @PathVariable("accountId") String accountId, @RequestBody CreditDTO creditDTO) {
        bankAccountService.credit(accountId, creditDTO.getAmount(), creditDTO.getDescription());
        return ResponseEntity.ok(creditDTO);
    }

    @PostMapping("/{clientId}/accounts/transfer")
    public ResponseEntity<ApiResponse<String>> transferFunds(@PathVariable("clientId") String clientId, @RequestBody TransferRequestDTO transferRequestDTO) {
        try {
            Long id = parseClientId(clientId);
            if (transferRequestDTO.getAmount() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, null, "Error: Transfer amount must be positive"));
            }
            bankAccountService.transfer(transferRequestDTO.getAccountSource(), transferRequestDTO.getAccountDestination(), transferRequestDTO.getAmount());
            return ResponseEntity.ok(new ApiResponse<>(true, "Transfert effectué avec succès.", null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: An unexpected error occurred during the transfer: " + e.getMessage()));
        }
    }

    @GetMapping("/{clientId}/beneficiaries")
    public ResponseEntity<List<BeneficiaryDTO>> getBeneficiaries(@PathVariable("clientId") String clientId) {
        List<BeneficiaryDTO> beneficiaries = bankAccountService.getBeneficiaries(clientId);
        return ResponseEntity.ok(beneficiaries);
    }

    @PostMapping("/{clientId}/beneficiaries")
    public ResponseEntity<BeneficiaryDTO> addBeneficiary(@PathVariable("clientId") String clientId, @RequestBody BeneficiaryDTO beneficiaryDTO) {
        BeneficiaryDTO savedBeneficiary = bankAccountService.addBeneficiary(clientId, beneficiaryDTO);
        return ResponseEntity.ok(savedBeneficiary);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
    }
}
/*
@RestController
@RequestMapping("/api/employee")
public class ClientController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping(path = "/clients")
    public List<ClientDTO> Clients(){
        return bankAccountService.listClients();
    }
    @GetMapping(path = "/clients/search")
    public List<ClientDTO> searchClients(@RequestParam(name = "keyword" , defaultValue = "") String keyword){
        return bankAccountService.searchClients(keyword);
    }

    @GetMapping(path = "/clients/{id}")
    public ClientDTO getClient(@PathVariable(name = "id") Long clientid) throws Exception {
        return bankAccountService.getClient(clientid);
    }

    // signifie que les données du Client vont etre recuperer a partir des données de la requete en format JSON
    //SERA MODIFIE APRES LATIFAH WORK, it doesn't work here diff approach
    @PostMapping(path = "/clients")
    public ClientDTO saveClient(@RequestBody ClientDTO clientDTO){
        return bankAccountService.saveClient(clientDTO);
    }

    @PutMapping("/clients/{ClientId}")
    public ClientDTO updateClient(@PathVariable(name="ClientId") Long clientId,@RequestBody ClientDTO clientDTO){
        clientDTO.setId(clientId);
        return bankAccountService.updateClient(clientDTO);
    }

    @DeleteMapping(path = "/clients/{ClientId}")
    public void deleteClient(@PathVariable Long clientId){
        bankAccountService.deleteClient(clientId);
    }
}

 */


