package ma.ensa.ebankingver1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import ma.ensa.ebankingver1.model.Currency;
import ma.ensa.ebankingver1.model.GlobalSetting;
import ma.ensa.ebankingver1.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    @Value("${currency.api.key}")
    private String apiKey;


    private String defaultCurrencyCode;
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    @PostConstruct
    public void init() {
        try {
            this.defaultCurrencyCode = getDefaultCurrency();
        } catch (IllegalStateException e) {
            // Loggez l'erreur et définissez une valeur par défaut si nécessaire
            this.defaultCurrencyCode = "MAD"; // Exemple de valeur par défaut
            logger.error("Aucune devise par défaut définie : {}", e.getMessage());

        }
    }

    /*
    public Currency saveCurrency(Currency currency) {
        return currencyRepository.save(currency);
    }

 */

    public double fetchExchangeRate(String currencyCode) {
        try {
            String url = "https://api.exchangerate-api.com/v4/latest/" + defaultCurrencyCode + "?access_key=" + apiKey;
            JsonNode rates = objectMapper.readTree(restTemplate.getForObject(url, String.class)).get("rates");

            if (rates != null && rates.has(currencyCode.toUpperCase())) {
                return rates.get(currencyCode.toUpperCase()).asDouble();
            } else {
                throw new IllegalArgumentException("Exchange rate for currency " + currencyCode + " not found.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch exchange rate: " + e.getMessage(), e);
        }
    }

public String getDefaultCurrency() {
    Currency defaultCurrency = currencyRepository.findByIsDefaultTrue();
    if (defaultCurrency == null) {
        throw new IllegalStateException("No default currency defined.");
    }
    return defaultCurrency.getCodeISO();}

    public Currency saveCurrency(Currency currency) {
        if (currencyRepository.existsByCodeISO(currency.getCodeISO())) {
            throw new IllegalArgumentException("Currency with code " + currency.getCodeISO() + " already exists");
        }

        // Auto-fetch exchange rate instead of validating
        double exchangeRate = fetchExchangeRate(currency.getCodeISO());
        currency.setExchangeRate(exchangeRate);

        // Handle default flag logic
        if (currency.getIsDefault()) {
            Currency existingDefault = currencyRepository.findByIsDefaultTrue();
            if (existingDefault != null) {
                existingDefault.setIsDefault(false);
                currencyRepository.save(existingDefault);
            }
        }

        return currencyRepository.save(currency);
    }
/*
public Currency saveCurrency(Currency currency) {
    if (currencyRepository.existsByCodeISO(currency.getCodeISO())) {
        throw new IllegalArgumentException("Currency with code " + currency.getCodeISO() + " already exists");
    }

    // Validation API
    if (!validateCurrencyWithAPI(currency.getCodeISO(), currency.getName())) {
        throw new IllegalArgumentException("Invalid currency code or not found in API: " + currency.getCodeISO());
    }
    // Vérifier si la devise est marquée comme default
    if (currency.getIsDefault()) {
        Currency existingDefault = currencyRepository.findByIsDefaultTrue();
        if (existingDefault != null) {
            existingDefault.setIsDefault(false);
            currencyRepository.save(existingDefault);
        }
    }

    return currencyRepository.save(currency);
}

 */
/*
    private boolean validateCurrencyWithAPI(String code, String name) {
        try {
            java.util.Currency.getInstance(code.toUpperCase()); // vérifie si code ISO 4217 valide

            String url = "https://api.exchangerate-api.com/v4/latest/" + getDefaultCurrency() + "?access_key=" + apiKey;
            JsonNode rates = objectMapper.readTree(restTemplate.getForObject(url, String.class)).get("rates");

            if (rates != null && rates.has(code.toUpperCase())) {
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        } catch (Exception e) {
            try {
                java.util.Currency.getInstance(code.toUpperCase()); // fallback ISO 4217
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
    }


 */
    public Currency updateCurrency(Long id, Currency currency) {
        Optional<Currency> existingCurrency = currencyRepository.findById(id);
        if (existingCurrency.isEmpty()) {
            throw new IllegalArgumentException("Currency not found");
        }
        // Si on essaie de mettre cette devise comme default
        if (currency.getIsDefault()) {
            Currency existingDefault = currencyRepository.findByIsDefaultTrue();
            if (existingDefault != null && !existingDefault.getId().equals(id)) {
                existingDefault.setIsDefault(false);
                currencyRepository.save(existingDefault);
            }
        }
        currency.setId(id);
        return currencyRepository.save(currency);
    }

    public void deleteCurrency(Long id) {
        if (!currencyRepository.existsById(id)) {
            throw new IllegalArgumentException("Currency not found");
        }
        currencyRepository.deleteById(id);
    }

    public Currency getCurrency(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Currency not found"));
    }
    public Currency updateExchangeRate(Long id, double exchangeRate) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Currency not found"));
        currency.setExchangeRate(exchangeRate);
        return currencyRepository.save(currency);
    }
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }


}
