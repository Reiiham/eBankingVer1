package ma.ensa.ebankingver1.DTO;

public class DebitDTO {
    private String accountId;
    private double amount;
    private String descritpion;
    //setters and getters
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
    public String getDescritpion() {
        return descritpion;
    }
    public void setDescritpion(String descritpion) {
        this.descritpion = descritpion;
    }

}
