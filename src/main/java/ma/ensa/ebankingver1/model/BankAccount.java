package ma.ensa.ebankingver1.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE" , length = 10)
public class BankAccount {
    @Id
    private String id;
    private double balance;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createAt;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @ManyToOne
    private User client;
    @OneToMany(mappedBy = "bankAccount" , fetch = FetchType.LAZY)
    private List<AccountOperation> accountOperations;
    public BankAccount() {
    }
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

    public User getClient() {
        return client;
    }
    public void setClient(User client) {
        this.client = client;
    }
    public List<AccountOperation> getAccountOperations() {
        return accountOperations;
    }
    public void setAccountOperations(List<AccountOperation> accountOperations) {
        this.accountOperations = accountOperations;
    }
    @Override
    public String toString() {
        return "BankAccount{" +
                "id='" + id + '\'' +
                ", balance=" + balance +
                ", createAt=" + createAt +
                ", status=" + status +
                ", client=" + client +
                ", accountOperations=" + accountOperations +
                '}';
    }

}