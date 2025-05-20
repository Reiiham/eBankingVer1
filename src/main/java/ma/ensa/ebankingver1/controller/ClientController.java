
package ma.ensa.ebankingver1.controller;

import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.service.BankAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final BankAccountService bankAccountService;

    public ClientController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/{clientId}/accounts")
    public ResponseEntity<List<BankAccountDTO>> getClientAccounts(@PathVariable("clientId") Long clientId) {
        List<BankAccountDTO> accounts = bankAccountService.getAccountsByClientId(clientId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{clientId}/accounts/{accountId}")
    public ResponseEntity<BankAccountDTO> getClientAccount(@PathVariable("clientId") Long clientId, @PathVariable("accountId") String accountId) {
        BankAccountDTO account = bankAccountService.getBankAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{clientId}/accounts/{accountId}/operations")
    public ResponseEntity<List<AccountOperationDTO>> getAccountOperations(@PathVariable("clientId") Long clientId, @PathVariable("accountId") String accountId) {
        List<AccountOperationDTO> operations = bankAccountService.AccountHistory(accountId);
        return ResponseEntity.ok(operations);
    }

    @PostMapping("/{clientId}/accounts/{accountId}/debit")
    public ResponseEntity<DebitDTO> debitAccount(@PathVariable("clientId") Long clientId, @PathVariable("accountId") String accountId, @RequestBody DebitDTO debitDTO) {
        bankAccountService.debit(accountId, debitDTO.getAmount(), debitDTO.getDescritpion());
        return ResponseEntity.ok(debitDTO);
    }

    @PostMapping("/{clientId}/accounts/{accountId}/credit")
    public ResponseEntity<CreditDTO> creditAccount(@PathVariable("clientId") Long clientId, @PathVariable("accountId") String accountId, @RequestBody CreditDTO creditDTO) {
        bankAccountService.credit(accountId, creditDTO.getAmount(), creditDTO.getDescritpion());
        return ResponseEntity.ok(creditDTO);
    }

    @PostMapping("/{clientId}/accounts/transfert")
    public ResponseEntity<String> transferFunds(@PathVariable("clientId") Long clientId, @RequestBody TransfertRequestDTO transfertRequestDTO) {
        bankAccountService.transfert(transfertRequestDTO.getAccountSource(), transfertRequestDTO.getAccountDestination(), transfertRequestDTO.getAmount());
        return ResponseEntity.ok("Transfert effectué avec succès.");
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


