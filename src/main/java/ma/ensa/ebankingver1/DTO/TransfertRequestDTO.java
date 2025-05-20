package ma.ensa.ebankingver1.DTO;

public class TransfertRequestDTO {
    private String accountSource;
    private String accountDestination;
    private double amount;

    // Getters and Setters
    public String getAccountSource() {
        return accountSource;
    }
    public void setAccountSource(String accountSource) {
        this.accountSource = accountSource;
    }
    public String getAccountDestination() {
        return accountDestination;
    }
    public void setAccountDestination(String accountDestination) {
        this.accountDestination = accountDestination;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }

}
