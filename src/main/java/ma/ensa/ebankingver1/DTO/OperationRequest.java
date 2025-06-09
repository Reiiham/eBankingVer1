package ma.ensa.ebankingver1.DTO;

public class OperationRequest {
    private String accountId;
    private double amount;

    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
}


