package ma.ensa.ebankingver1.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CryptoRateService {

    @Autowired
    private RestTemplate restTemplate;

    private static final List<String> SYMBOLS = List.of("BTCUSDT","ETHUSDT");

    public double getRate(String symbol) {
        String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + symbol;
        Map<String,String> resp = restTemplate.getForObject(url, Map.class);
        return Double.parseDouble(resp.get("price"));
    }

    public Map<String, Double> getAllRates() {
        return SYMBOLS.stream()
                .collect(Collectors.toMap(s -> s, this::getRate));
    }
}
