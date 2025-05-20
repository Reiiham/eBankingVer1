package ma.ensa.ebankingver1.DTO;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import ma.ensa.ebankingver1.model.AccountStatus;

import java.util.Date;

public class SavingBankAccountDTO extends BankAccountDTO {
    private String id;
    private double balance;
    private Date createAt;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    private ClientDTO client;
    private double interestRate;
    private String Type;
    //setters and getters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public Date getCreateAt() {
        return createAt;
    }
    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }
    public AccountStatus getStatus() {
        return status;
    }
    public void setStatus(AccountStatus status) {
        this.status = status;
    }
    public ClientDTO getClient() {
        return client;
    }
    public void setClient(ClientDTO client) {
        this.client = client;
    }
    public double getInterestRate() {
        return interestRate;
    }
    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }
    public String getType() {
        return Type;
    }
    public void setType(String type) {
        Type = type;
    }


}
