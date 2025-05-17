package ma.ensa.ebankingver1.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.ensa.ebankingver1.filter.JwtFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan({
        "ma.ensa.ebankingver1.config",
        "ma.ensa.ebankingver1.service",
        "ma.ensa.ebankingver1.filter",
        "ma.ensa.ebankingver1.controller",
        "ma.ensa.ebankingver1" // pour être sûr
})
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}