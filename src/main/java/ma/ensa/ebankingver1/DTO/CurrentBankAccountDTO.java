package ma.ensa.ebankingver1.DTO;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import ma.ensa.ebankingver1.model.AccountStatus;

import java.util.Date;

public class CurrentBankAccountDTO extends BankAccountDTO {
    private String id;
    private double balance;
    private Date createAt;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    private ClientDTO client;
    private double overDraft;

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
    public double getOverDraft() {
        return overDraft;
    }
    public void setOverDraft(double overDraft) {
        this.overDraft = overDraft;
    }

}
