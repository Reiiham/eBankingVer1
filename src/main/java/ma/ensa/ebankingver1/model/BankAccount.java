package ma.ensa.ebankingver1.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.util.*;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "accounts")
public class BankAccount {
    @Id
    private String id;

    @PrePersist
    public void generateIdAndPIN() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.transactionPIN == null) {
            this.transactionPIN = generateFourDigitPIN();
        }
    }

    // Encrypted, stored in DB
    @Column(name = "accountnumber")
    private String accountNumber;

    // Human-readable format, for display/search
    @Transient
    private String rawAccountNumber;

    @Column(name = "rib", nullable = false, unique = true)
    private String rib;


    private String type; // courant / épargne
    private double balance;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    private List<Transaction> transactions;

    @Column(name = "transaction_pin")
    private String transactionPIN;

    private String generateFourDigitPIN() {
        int pin = new Random().nextInt(9000) + 1000; // 1000 to 9999
        return String.valueOf(pin);
    }

    // getters and setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    public String getRawAccountNumber() {
        return rawAccountNumber;
    }
    public void setRawAccountNumber(String rawAccountNumber) {
        this.rawAccountNumber = rawAccountNumber;
    }
    public String getRib() {
        return rib;
    }
    public void setRib(String rib) {
        this.rib = rib;
    }
    public String getTransactionPIN() { return transactionPIN; }
    public void setTransactionPIN(String transactionPIN) { this.transactionPIN = transactionPIN; }
}