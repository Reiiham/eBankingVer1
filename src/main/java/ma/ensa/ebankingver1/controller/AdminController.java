package ma.ensa.ebankingver1.controller;

import jakarta.validation.Valid;
import ma.ensa.ebankingver1.DTO.ExchangeRateUpdateRequest;
import ma.ensa.ebankingver1.DTO.SettingUpdateRequest;
import ma.ensa.ebankingver1.model.Currency;
import ma.ensa.ebankingver1.model.GlobalSetting;
import ma.ensa.ebankingver1.service.CurrencyService;
import ma.ensa.ebankingver1.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

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
    public Currency updateCurrency(@PathVariable ("id") Long id, @RequestBody Currency currency) {
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

 */
    //pour utiliser que json
    // MARCHE ET RETOURNE JSON
    @PutMapping("/settings/{key}")
@PreAuthorize("hasRole('ADMIN')")
public GlobalSetting updateSetting(@PathVariable (name = "key") String key, @RequestBody SettingUpdateRequest request) {
    return globalSettingService.updateSetting(key, request.getValue());
}
//delete setting doesn't make sense donc je l ai pas fait

}