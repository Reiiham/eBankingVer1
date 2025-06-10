package ma.ensa.ebankingver1.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class QRPaymentRequest {

    private String sourceAccountId;

    @NotBlank(message = "RIB cannot be blank")
    private String rib;

    @Min(value = 0, message = "Amount must be positive")
    private double amount;

    private String description;

    // Getters and setters
    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(String sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}