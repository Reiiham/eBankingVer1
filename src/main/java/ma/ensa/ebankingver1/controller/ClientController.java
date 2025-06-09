
package ma.ensa.ebankingver1.controller;

import jakarta.validation.Valid;
import ma.ensa.ebankingver1.DTO.BeneficiaryResponseDTO;
import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.DTO.TransferRequest;
import ma.ensa.ebankingver1.DTO.TransferResponse;
import ma.ensa.ebankingver1.model.*;
import ma.ensa.ebankingver1.service.BeneficiaryService;
import ma.ensa.ebankingver1.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import ma.ensa.ebankingver1.service.BankAccountService;
import ma.ensa.ebankingver1.service.TransactionService;
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private BeneficiaryService beneficiaryService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;
    /**
     * Effectuer un virement entre comptes
     */
    @PostMapping("/{clientId}/services/virement")
    public ResponseEntity<?> executeTransfer(
            @PathVariable("clientId") Long clientId,
            @RequestBody TransferRequest transferRequest,
            Authentication authentication) {

        try {
            // Vérifier que l'utilisateur connecté correspond au client
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            // DEBUG: Afficher les services actifs
            System.out.println("Services actifs du client: " + currentUser.getServicesActifs());

            // Vérifier que le service VIREMENT est actif pour ce client (vérification flexible)
            List<String> servicesActifs = currentUser.getServicesActifs();
            boolean virementActif = servicesActifs != null && (
                    servicesActifs.contains("VIREMENT") ||
                            servicesActifs.contains("VIREMENTS") ||  // Ajout du pluriel
                            servicesActifs.contains("virement") ||
                            servicesActifs.contains("virements") ||   // Ajout du pluriel en minuscule
                            servicesActifs.contains("Virement") ||
                            servicesActifs.contains("Virements") ||   // Ajout du pluriel avec majuscule
                            servicesActifs.stream().anyMatch(service ->
                                    service != null && (service.toUpperCase().equals("VIREMENT") ||
                                            service.toUpperCase().equals("VIREMENTS")))
            );

            if (!virementActif) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Service VIREMENT non activé pour ce client. Services actifs: " + servicesActifs));
            }

            // Vérifier que le compte source appartient au client
            BankAccount sourceAccount = bankAccountService.findById(transferRequest.getFromAccountId());
            if (sourceAccount == null || !sourceAccount.getUser().getId().equals(clientId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Compte source invalide"));
            }

            // Vérifier le solde suffisant
            if (sourceAccount.getBalance() < transferRequest.getAmount()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Solde insuffisant"));
            }

            // Vérifier le montant positif
            if (transferRequest.getAmount() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le montant doit être positif"));
            }

            // Trouver le compte destinataire par RIB
            BankAccount destinationAccount = bankAccountService.findByRib(transferRequest.getToRib());
            if (destinationAccount == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Compte destinataire introuvable"));
            }

            // Effectuer le virement
            String transactionId = performTransfer(sourceAccount, destinationAccount,
                    transferRequest.getAmount(), transferRequest.getDescription(),
                    transferRequest.getBeneficiaryName(), currentUser);

            return ResponseEntity.ok(new TransferResponse(
                    true,
                    "Virement effectué avec succès",
                    transactionId,
                    sourceAccount.getBalance()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du virement: " + e.getMessage()));
        }
    }

    /**
     * Obtenir l'historique des virements d'un client
     */
    @GetMapping("/{clientId}/services/virement/history")
    public ResponseEntity<?> getTransferHistory(
            @PathVariable("clientId") Long clientId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            List<Transaction> transfers = transactionService.findTransfersByUserId(clientId, page, size);

            // Convertir en DTO pour éviter la sérialisation circulaire
            List<TransactionHistoryDTO> transferHistory = transfers.stream()
                    .map(TransactionHistoryDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(transferHistory);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de l'historique"));
        }
    }

    /**
     * Valider un RIB destinataire
     */
    @PostMapping("/{clientId}/services/virement/validate-rib")
    public ResponseEntity<?> validateRib(
            @PathVariable("clientId") Long clientId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            String rib = request.get("rib");
            BankAccount account = bankAccountService.findByRib(rib);

            if (account != null) {
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "accountHolder", account.getUser().getFirstName() + " " + account.getUser().getLastName(),
                        "accountType", account.getType()
                ));
            } else {
                return ResponseEntity.ok(Map.of("valid", false));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la validation du RIB"));
        }
    }

    /**
     * Obtenir les comptes du client pour les virements
     */
    @GetMapping("/{clientId}/services/virement/accounts")
    public ResponseEntity<?> getClientAccountsForTransfer(
            @PathVariable("clientId") Long clientId,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            List<BankAccount> accounts = currentUser.getAccounts();
            return ResponseEntity.ok(accounts);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des comptes"));
        }
    }

    /**
     * Méthode privée pour effectuer le virement
     */
    private String performTransfer(BankAccount sourceAccount, BankAccount destinationAccount,
                                   double amount, String description, String beneficiaryName, User user) {

        // Générer un ID de transaction unique
        String transactionId = UUID.randomUUID().toString();

        // Débiter le compte source
        sourceAccount.setBalance(sourceAccount.getBalance() - amount);
        bankAccountService.save(sourceAccount);

        // Créditer le compte destination
        destinationAccount.setBalance(destinationAccount.getBalance() + amount);
        bankAccountService.save(destinationAccount);

        // Créer la transaction de débit
        Transaction debitTransaction = new Transaction();
        debitTransaction.setId(transactionId + "_DEBIT");
        debitTransaction.setType("VIREMENT_SORTANT");
        debitTransaction.setAmount(-amount);
        debitTransaction.setDate(LocalDateTime.now());
        debitTransaction.setAccount(sourceAccount);
        debitTransaction.setUser(user);
        transactionService.save(debitTransaction);

        // Créer la transaction de crédit
        Transaction creditTransaction = new Transaction();
        creditTransaction.setId(transactionId + "_CREDIT");
        creditTransaction.setType("VIREMENT_ENTRANT");
        creditTransaction.setAmount(amount);
        creditTransaction.setDate(LocalDateTime.now());
        creditTransaction.setAccount(destinationAccount);
        creditTransaction.setUser(destinationAccount.getUser());
        transactionService.save(creditTransaction);

        return transactionId;
    }
    /**
     * Obtenir tous les bénéficiaires d'un client
     */
    @GetMapping("/{clientId}/beneficiaries")
    public ResponseEntity<?> getBeneficiaries(
            @PathVariable("clientId") Long clientId,
            @RequestParam(value = "actif", required = false) Boolean actif,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            List<Beneficiary> beneficiaries = beneficiaryService.findByUserIdAndActif(clientId, actif);
            List<BeneficiaryResponseDTO> beneficiaryDTOs = beneficiaries.stream()
                    .map(BeneficiaryResponseDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(beneficiaryDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des bénéficiaires"));
        }
    }

    /**
     * Ajouter un nouveau bénéficiaire
     */
    @PostMapping("/{clientId}/beneficiaries")
    public ResponseEntity<?> addBeneficiary(
            @PathVariable("clientId") Long clientId,
            @Valid @RequestBody CreateBeneficiaryRequest request,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            // Vérifier que le RIB existe et est valide
            BankAccount targetAccount = bankAccountService.findByRib(request.getRib());
            if (targetAccount == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "RIB invalide ou compte inexistant"));
            }

            // Vérifier que l'utilisateur n'ajoute pas son propre compte
            if (targetAccount.getUser().getId().equals(clientId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vous ne pouvez pas vous ajouter comme bénéficiaire"));
            }

            // Vérifier que le bénéficiaire n'existe pas déjà
            if (beneficiaryService.existsByUserIdAndRib(clientId, request.getRib())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ce bénéficiaire existe déjà"));
            }

            Beneficiary beneficiary = beneficiaryService.createBeneficiary(currentUser, request);
            return ResponseEntity.ok(new BeneficiaryResponseDTO(beneficiary));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'ajout du bénéficiaire: " + e.getMessage()));
        }
    }

    /**
     * Modifier un bénéficiaire
     */
    @PutMapping("/{clientId}/beneficiaries/{beneficiaryId}")
    public ResponseEntity<?> updateBeneficiary(
            @PathVariable("clientId") Long clientId,
            @PathVariable("beneficiaryId") Long beneficiaryId,
            @Valid @RequestBody UpdateBeneficiaryRequest request,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            Beneficiary beneficiary = beneficiaryService.findByIdAndUserId(beneficiaryId, clientId);
            if (beneficiary == null) {
                return ResponseEntity.notFound().build();
            }

            Beneficiary updatedBeneficiary = beneficiaryService.updateBeneficiary(beneficiary, request);
            return ResponseEntity.ok(new BeneficiaryResponseDTO(updatedBeneficiary));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la modification du bénéficiaire"));
        }
    }

    /**
     * Supprimer (désactiver) un bénéficiaire
     */
    @DeleteMapping("/{clientId}/beneficiaries/{beneficiaryId}")
    public ResponseEntity<?> deleteBeneficiary(
            @PathVariable("clientId") Long clientId,
            @PathVariable("beneficiaryId") Long beneficiaryId,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            Beneficiary beneficiary = beneficiaryService.findByIdAndUserId(beneficiaryId, clientId);
            if (beneficiary == null) {
                return ResponseEntity.notFound().build();
            }

            beneficiaryService.deactivateBeneficiary(beneficiary);
            return ResponseEntity.ok(Map.of("message", "Bénéficiaire supprimé avec succès"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la suppression du bénéficiaire"));
        }
    }

    /**
     * Obtenir les détails d'un bénéficiaire spécifique
     */
    @GetMapping("/{clientId}/beneficiaries/{beneficiaryId}")
    public ResponseEntity<?> getBeneficiary(
            @PathVariable("clientId") Long clientId,
            @PathVariable("beneficiaryId") Long beneficiaryId,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            Beneficiary beneficiary = beneficiaryService.findByIdAndUserId(beneficiaryId, clientId);
            if (beneficiary == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(new BeneficiaryResponseDTO(beneficiary));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération du bénéficiaire"));
        }
    }

    /**
     * Rechercher des bénéficiaires par nom
     */
    @GetMapping("/{clientId}/beneficiaries/search")
    public ResponseEntity<?> searchBeneficiaries(
            @PathVariable("clientId") Long clientId,
            @RequestParam("query") String query,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            List<Beneficiary> beneficiaries = beneficiaryService.searchBeneficiaries(clientId, query);
            List<BeneficiaryResponseDTO> beneficiaryDTOs = beneficiaries.stream()
                    .map(BeneficiaryResponseDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(beneficiaryDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la recherche des bénéficiaires"));
        }
    }

    /**
     * Obtenir les types de relation disponibles
     */
    @GetMapping("/{clientId}/beneficiaries/relation-types")
    public ResponseEntity<?> getRelationTypes(
            @PathVariable("clientId") Long clientId,
            Authentication authentication) {

        try {
            User currentUser = userService.findByUsername(authentication.getName());
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Accès non autorisé"));
            }

            Map<String, String> relationTypes = Arrays.stream(RelationType.values())
                    .collect(Collectors.toMap(
                            RelationType::name,
                            RelationType::getDisplayName
                    ));

            return ResponseEntity.ok(relationTypes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des types de relation"));
        }
    }
}