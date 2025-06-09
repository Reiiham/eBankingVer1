package ma.ensa.ebankingver1.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

public class AddAccountRequest {

    @NotNull(message = "L'ID du client est obligatoire")
    private Long clientId;

    @NotNull(message = "Le type de compte est obligatoire")
    @Pattern(regexp = "^(courant|epargne)$", message = "Le type de compte doit être 'courant' ou 'epargne'")
    private String type;

    @NotNull(message = "Le solde est obligatoire")
    @Positive(message = "Le solde doit être positif")
    private Double balance;

    // Constructeurs
    public AddAccountRequest() {}

    public AddAccountRequest(Long clientId, String type, Double balance) {
        this.clientId = clientId;
        this.type = type;
        this.balance = balance;
    }

    // Getters et Setters
    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "AddAccountRequest{" +
                "clientId=" + clientId +
                ", type='" + type + '\'' +
                ", balance=" + balance +
                '}';
    }
}