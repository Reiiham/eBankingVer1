package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.model.Currency;
import ma.ensa.ebankingver1.model.GlobalSetting;
import ma.ensa.ebankingver1.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;

    public Currency saveCurrency(Currency currency) {
        return currencyRepository.save(currency);
    }

    public Currency updateCurrency(Long id, Currency currency) {
        Optional<Currency> existingCurrency = currencyRepository.findById(id);
        if (existingCurrency.isEmpty()) {
            throw new IllegalArgumentException("Currency not found");
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
