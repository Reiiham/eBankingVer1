package ma.ensa.ebankingver1.controller;
import ma.ensa.ebankingver1.DTO.ClientUpdateRequest;
import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.DTO.SecureActionRequest;
import ma.ensa.ebankingver1.DTO.ServiceSuspensionRequest;
import ma.ensa.ebankingver1.model.*;
import ma.ensa.ebankingver1.repository.SuspendedServiceRepository;
import ma.ensa.ebankingver1.repository.UserRepository;
import ma.ensa.ebankingver1.service.BankAccountService;
import ma.ensa.ebankingver1.service.ClientService;
import ma.ensa.ebankingver1.service.EnrollmentService;
import ma.ensa.ebankingver1.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/employee")
public class EmployeeDashboardController {

    @Autowired
    private EnrollmentService service;

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SuspendedServiceRepository suspendedServiceRepository;

    private final BankAccountService bankAccountService;
    private final TransactionService transactionService;

    public EmployeeDashboardController(BankAccountService bankAccountService, TransactionService transactionService) {
        this.bankAccountService = bankAccountService;
        this.transactionService = transactionService;
    }


    @GetMapping("/clients/count")
    public long countClients() {
        return service.countClients();
    }

    @GetMapping("/accounts/count")
    public long countAccounts() {
        return service.countAccounts();
    }

    // for the lists we have a simple list with just name and id à premiere vue (/clients/basic)
    //then when we click on the name we have //clients/{id} to display the client's details
    // then for the search bar it submits to /clients/search
    //and then if the employee wants the full list of clients with all of the details displayed at
    //once, it's /clients/detailed (idk if this is aesthetically pleasing tho, u choose)


    //endpoint for the simple list of all clients
    @GetMapping("/clients/basic")
    public List<ClientBasicDTO> listClientsBasic() {
        return clientService.getAllClientsBasic();
    }

    //endpoint for the list of all clients with details
    @GetMapping("/clients/detailed")
    public List<ClientSummaryDTO> listClients(@RequestParam(name = "search", required = false) String search) {
        return clientService.getClientsWithAccountsAndTransactions(search);
    }

    //endpoint to search client by name
    @GetMapping("/clients/search")
    public List<ClientSummaryDTO> searchClientsByName(@RequestParam("name") String name) {
        return clientService.searchClientsByName(name);
    }

//    //recherche par numeros de comptes
//    @GetMapping("/accounts/search")
//    public List<AccountSummaryDTO> searchAccounts(@RequestParam("query") String query) {
//        return clientService.searchAccountsByRawNumber(query);
//    }

    //endpoint pour afficher les details d'un client
    @GetMapping("/clients/{id}")
    public ClientSummaryDTO getClientDetails(@PathVariable("id") Long id) {
        return clientService.getClientWithDetails(id);
    }

    @PostMapping("/enroll")
    public void enroll(@RequestBody EnrollmentRequest req) {
        service.enrollClient(req);
    }

//    @PutMapping("/update")
//    public void update(@RequestBody ClientUpdateRequest req) {
//        boolean ok = service.updateClient(req);
//        if (!ok) {
//            throw new RuntimeException("Update failed: invalid supervisor password or client not found.");
//        }
//    }

    //update now requires a supervisor pw
    @PutMapping("/update")
    public ResponseEntity<String> update(@RequestBody ClientUpdateRequest req) {
        if (!service.validateSupervisor(req.getSupervisorCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Code superviseur incorrect.");
        }
        boolean ok = service.updateClient(req);
        if (!ok) {
            return ResponseEntity.badRequest().body("Échec de la mise à jour : client introuvable ou données invalides.");
        }
        return ResponseEntity.ok("Client mis à jour avec succès.");
    }


//    @DeleteMapping("/delete")
//    public void delete(@RequestBody ClientDeletionRequest req) {
//        boolean ok = service.deleteClient(req.getClientId());
//        if (!ok) {
//            throw new RuntimeException("Deletion failed: client has active transactions or does not exist.");
//        }
//    }

    // on a changé le endpoint pour delete, maintenant, it require a supervisor password
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestBody SecureActionRequest req) {
        if (!service.validateSupervisor(req.getSupervisorCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Code superviseur incorrect.");
        }
        boolean ok = service.deleteClient(req.getClientId());
        if (!ok) {
            return ResponseEntity.badRequest().body("Échec de la suppression : client introuvable ou a des transactions actives.");
        }
        return ResponseEntity.ok("Client supprimé avec succès.");
    }

    // Endpoint pour toggle compteBloque et documentsComplets
    @PutMapping("/clients/{id}/status")
    public ResponseEntity<String> updateClientStatus(
            @PathVariable("id") Long id,
            @RequestParam("compteBloque") Boolean compteBloque,
            @RequestParam("documentsComplets") Boolean documentsComplets) {

        try {
            clientService.updateClientStatus(id, compteBloque, documentsComplets);
            return ResponseEntity.ok("Statuts mis à jour avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //endpoint pour activer un service donné
    //exemple d'entree
//    {
//        "clientId": "3",
//            "services": ["VIREMENT", "CHEQUIER"]
//    }
    //NB il faut que l'admin ajoute la liste des services ou bien que ca soit un enum
    @PostMapping("/clients/activer-services")
    public ResponseEntity<?> activerServicesPourClient(@RequestBody ActivateServicesRequest request) {
        try {
            User updatedUser = clientService.activerServices(request.getClientId(), request.getServices());
            return ResponseEntity.ok("Services activés avec succès pour " + updatedUser.getFirstName());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //endpoint pour la suspension d'un client
    //BankService est un enum donc en front on aura une liste dépliante to select from
    //exemple d'entree:
//    {
//        "clientId": 1,
//            "servicesToSuspend": ["VIREMENT", "CHEQUIER"],
//        "reason": "Non-paiement",
//            "notificationMessage": "Vos services sont suspendus pour non-paiement"
//    }
    @PutMapping("/clients/{id}/suspend-services")
    public ResponseEntity<String> suspendServices(
            @PathVariable("id") Long id,
            @RequestBody ServiceSuspensionRequest request) {

        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        User user = userOptional.get();

        for (BankService service : request.getServicesToSuspend()) {
            SuspendedService suspended = new SuspendedService();
            suspended.setServiceName(service.name()); // On enregistre le nom de l'enum en base
            suspended.setReason(request.getReason());
            suspended.setNotificationMessage(request.getNotificationMessage());
            suspended.setUser(user);

            user.getSuspendedServices().add(suspended);
        }

        userRepository.save(user);

        return ResponseEntity.ok("Services suspendus avec succès");
    }


    //réactive un service desactivé
    //   exemple d'entree:
    //["VIREMENT", "CHEQUIER"]
    @PutMapping("/clients/{id}/reactivate-services")
    public ResponseEntity<String> reactivateServices(
            @PathVariable("id") Long id,
            @RequestBody List<String> servicesToReactivate) {

        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        User user = userOptional.get();

        // Convertir les chaînes en enums de manière sûre
        List<BankService> validServices = servicesToReactivate.stream()
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .filter(name -> {
                    try {
                        BankService.valueOf(name);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                })
                .map(BankService::valueOf)
                .toList();

        List<SuspendedService> currentSuspended = user.getSuspendedServices();
        List<SuspendedService> toRemove = currentSuspended.stream()
                .filter(s -> validServices.contains(s.getServiceName()))
                .toList();

        currentSuspended.removeAll(toRemove);
        suspendedServiceRepository.deleteAll(toRemove);

        userRepository.save(user);

        return ResponseEntity.ok("Services réactivés avec succès");
    }

    @PostMapping("/employee/transfer")
    public ResponseEntity<String> transferFunds(@RequestBody TransferRequest request) {
        try {
            BankAccount from = bankAccountService.findById(request.getFromAccountId());
            BankAccount to = bankAccountService.findByRib(request.getToRib());

            if (from == null || to == null) {
                return ResponseEntity.badRequest().body("Compte source ou destinataire introuvable");
            }

            if (from.getBalance() < request.getAmount()) {
                return ResponseEntity.badRequest().body("Solde insuffisant");
            }

            if (request.getAmount() <= 0) {
                return ResponseEntity.badRequest().body("Le montant doit être positif");
            }

            // Débit/crédit
            from.setBalance(from.getBalance() - request.getAmount());
            to.setBalance(to.getBalance() + request.getAmount());

            bankAccountService.save(from);
            bankAccountService.save(to);

            // Transactions
            Transaction debit = new Transaction();
            debit.setId(java.util.UUID.randomUUID().toString() + "_DEBIT");
            debit.setAmount(-request.getAmount());
            debit.setType("VIREMENT_SORTANT");
            debit.setDate(java.time.LocalDateTime.now());
            debit.setAccount(from);
            debit.setUser(from.getUser());

            Transaction credit = new Transaction();
            credit.setId(java.util.UUID.randomUUID().toString() + "_CREDIT");
            credit.setAmount(request.getAmount());
            credit.setType("VIREMENT_ENTRANT");
            credit.setDate(java.time.LocalDateTime.now());
            credit.setAccount(to);
            credit.setUser(to.getUser());

            transactionService.save(debit);
            transactionService.save(credit);

            return ResponseEntity.ok("✅ Virement effectué avec succès.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du virement : " + e.getMessage());
        }
    }

    @PostMapping("/operations/deposit")
    public ResponseEntity<?> deposit(@RequestBody OperationRequest request) {
        BankAccount account = bankAccountService.findById(request.getAccountId());
        if (account == null) return ResponseEntity.badRequest().body("Compte introuvable");
        if (request.getAmount() <= 0) return ResponseEntity.badRequest().body("Montant invalide");

        account.setBalance(account.getBalance() + request.getAmount());
        bankAccountService.save(account);

        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setAccount(account);
        tx.setUser(account.getUser());
        tx.setAmount(request.getAmount());
        tx.setType("DEPOT");
        tx.setDate(LocalDateTime.now());
        transactionService.save(tx);

        return ResponseEntity.ok("Dépôt effectué avec succès");
    }

    @PostMapping("/operations/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody OperationRequest request) {
        BankAccount account = bankAccountService.findById(request.getAccountId());
        if (account == null) return ResponseEntity.badRequest().body("Compte introuvable");
        if (request.getAmount() <= 0 || request.getAmount() > account.getBalance())
            return ResponseEntity.badRequest().body("Montant invalide ou solde insuffisant");

        account.setBalance(account.getBalance() - request.getAmount());
        bankAccountService.save(account);

        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setAccount(account);
        tx.setUser(account.getUser());
        tx.setAmount(-request.getAmount());
        tx.setType("RETRAIT");
        tx.setDate(LocalDateTime.now());
        transactionService.save(tx);

        return ResponseEntity.ok("Retrait effectué avec succès");
    }

    /**
     * Endpoint pour ajouter un nouveau compte bancaire à un client existant
     * Exemple d'entrée: {
     *   "clientId": 2,
     *   "type": "epargne",
     *   "balance": 1000.0
     * }
     */
    @PostMapping("/clients/add-account")
    public ResponseEntity<?> addBankAccount(@RequestBody AddAccountRequest request) {
        try {
            BankAccount newAccount = service.addBankAccount(request);

            // Créer une réponse avec les informations du compte créé
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Compte bancaire créé avec succès",
                    "accountId", newAccount.getId(),
                    "accountNumber", newAccount.getRawAccountNumber(),
                    "rib", newAccount.getRib(),
                    "type", newAccount.getType(),
                    "balance", newAccount.getBalance(),
                    "transactionPIN", newAccount.getTransactionPin()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Erreur lors de la création du compte", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne du serveur", "message", "Une erreur inattendue s'est produite"));
        }
    }

}



