package ma.ensa.ebankingver1.controller;

import jakarta.validation.Valid;
import ma.ensa.ebankingver1.DTO.EmployeeDTO;
import ma.ensa.ebankingver1.DTO.ExchangeRateUpdateRequest;
import ma.ensa.ebankingver1.DTO.SettingUpdateRequest;
import ma.ensa.ebankingver1.model.Currency;
import ma.ensa.ebankingver1.model.GlobalSetting;
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.service.CurrencyService;
import ma.ensa.ebankingver1.service.EmployeeService;
import ma.ensa.ebankingver1.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private SettingService settingService; // Injection de SettingService
    @Autowired
    private EmployeeService employeeService;


    @GetMapping("/currencies")
    public ResponseEntity<List<Currency>> getCurrencies() {
        List<Currency> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(currencies);
    }

    @PostMapping("/currencies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Currency> addCurrency(@RequestBody Currency currency) {
        try {
            Currency savedCurrency = currencyService.addCurrency(currency);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCurrency); // Retourne 201 si succès
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // Retourne 409 en cas de conflit
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retourne 500 en cas d'erreur
        }
    }
    @PutMapping("/currencies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Currency> updateCurrency(@PathVariable("id") Long id, @RequestBody Currency currency) {
        try {
            Currency updatedCurrency = currencyService.updateCurrency(id, currency);
            return ResponseEntity.ok(updatedCurrency); // Retourne 200 si succès
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Retourne 404 si la devise n'existe pas
        }
    }


    @PutMapping("/currencies/{id}/exchange-rate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Currency> updateExchangeRate(@PathVariable("id") Long id, @Valid @RequestBody ExchangeRateUpdateRequest request) {
        try {
            Currency updatedCurrency = currencyService.updateExchangeRate(id, request.getExchangeRate());
            return ResponseEntity.ok(updatedCurrency); // Retourne 200 si succès
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Retourne 404 si la devise n'existe pas
        }
    }

    @DeleteMapping("/currencies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCurrency(@PathVariable("id") Long id) {
        try {
            currencyService.deleteCurrency(id);
            return ResponseEntity.noContent().build(); // Retourne 204 si succès
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Retourne 404 si la devise n'existe pas
        }
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats() {
        return Map.of(
                "totalCurrencies", currencyService.getAllCurrencies().size(),
                "totalSettings", settingService.getAllSettings().size(),
                "lastUpdate", LocalDateTime.now().toString()
        );
    }
    @PostMapping("/employees")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        try {
            User employee = employeeService.createEmployee(employeeDTO);
            return new ResponseEntity<>(employee, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne");
        }
    }

    /*

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private SettingService globalSettingService;
    @Autowired
    private SettingService settingService;

    // Gestion des devises
    //get currencies
    //MARCHE
    @GetMapping("/currencies")
    public List<Currency> getCurrencies() {
        return currencyService.getAllCurrencies();
    }

    //MARCHE
    @PostMapping("/currencies")
    //@PreAuthorize("hasRole('ADMIN')")
    public Currency addCurrency(@RequestBody Currency currency) {
        return currencyService.saveCurrency(currency);
    }

    //MARCHE
    @PutMapping("/currencies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Currency updateCurrency(@PathVariable("id") Long id, @RequestBody Currency currency) {
        return currencyService.updateCurrency(id, currency);
    }

    // MARCHE ET RETOURNE JSON
    //n est pas encore ajouté dans angular
    @PutMapping("/currencies/{id}/exchange-rate")
    @PreAuthorize("hasRole('ADMIN')")
    public Currency updateExchangeRate(@PathVariable("id") Long id, @Valid @RequestBody ExchangeRateUpdateRequest request) {
        return currencyService.updateExchangeRate(id, request.getExchangeRate());
    }
//ne retourne rien mais marche

    @DeleteMapping("/currencies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCurrency(@PathVariable("id") Long id) {
        currencyService.deleteCurrency(id);
    }

    // Gestion des paramètres globaux
    //MARCHE
    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public List<GlobalSetting> getAllSettings() {
        return globalSettingService.getAllSettings();
    }
//il retourne un true text brut mais on va utiliser angular apres donc json



    // marche
@GetMapping("/settings/{key}")
@PreAuthorize("hasRole('ADMIN')")
public Map<String, String> getSettingValue(@PathVariable(name = "key") String key) {
    return Map.of("value", globalSettingService.getSettingValue(key));
}

//endpoint pour dashbord
@GetMapping("/dashboard")
public Map<String, Object> getDashboardStats() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalCurrencies", currencyService.getAllCurrencies().size());
    stats.put("totalSettings", settingService.getAllSettings().size());
    stats.put("lastUpdate", LocalDate.now().toString());
    return stats;
}




    /*
    @PutMapping("/settings/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalSetting updateSetting(@PathVariable String key, @RequestBody String value) {
        return globalSettingService.updateSetting(key, value);
    }


    //pour utiliser que json
    // MARCHE ET RETOURNE JSON
    @PutMapping("/settings/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalSetting updateSetting(@PathVariable (name = "key") String key, @RequestBody SettingUpdateRequest request) {
          return globalSettingService.updateSetting(key, request.getValue());
}
*/

}