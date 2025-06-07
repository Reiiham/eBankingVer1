package ma.ensa.ebankingver1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", unique = true, nullable = false)
    private String username;


    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "Tel")
    private String Tel;

    @Column(name = "Birth_Date")
    private LocalDate Birth_Date;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;
    @Column(name = "password")
    private String password;


    @Column(name = "must_change_password")
    private Boolean mustChangePassword;
    @Column(unique = true, nullable = false)
    private String cin;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Beneficiary> beneficiaries = new ArrayList<>();

    public List<Beneficiary> getBeneficiaries() { return beneficiaries; }
    public void setBeneficiaries(List<Beneficiary> beneficiaries) { this.beneficiaries = beneficiaries; }

    // Getters et Setters
    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    @Column(name = "compte_bloque")
    private Boolean compteBloque;
    @Column(name = "documents_complets")
    private Boolean documentsComplets;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> servicesActifs = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<BankAccount> accounts;

    public boolean isCompteBloque() { return compteBloque; }
    public boolean isDocumentsComplets() { return documentsComplets; }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SuspendedService> suspendedServices = new ArrayList<>();




/*
    @OneToMany(mappedBy = "user")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<BankAccount> bankAccounts;


 */
    @Column(name = "token")
    private String token;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    // Getters et setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(LocalDateTime tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }


    public User() {
    }
    public List<String> getServicesActifs() { return servicesActifs; }
    public void setServicesActifs(List<String> servicesActifs) { this.servicesActifs = servicesActifs; }

    public List<BankAccount> getAccounts() { return accounts; }
    public void setAccounts(List<BankAccount> accounts) { this.accounts = accounts; }

    public Boolean getCompteBloque() { return compteBloque; }
    public void setCompteBloque(boolean compteBloque) {
        this.compteBloque = compteBloque;
    }

    public Boolean getDocumentsComplets() { return documentsComplets; }
    public void setDocumentsComplets(boolean documentsComplets) {
        this.documentsComplets = documentsComplets;
    }

    public List<SuspendedService> getSuspendedServices() {
        return suspendedServices;
    }

    public void setSuspendedServices(List<SuspendedService> suspendedServices) {
        this.suspendedServices = suspendedServices;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return Tel;
    }

    public void setTel(String tel) {
        this.Tel=tel;
    }

    public LocalDate getBirth_Date() {
        return Birth_Date;
    }

    public void setBirth_Date(LocalDate birth_Date) {
        this.Birth_Date = birth_Date;
    }

    public Role getRole() {
        return role;
    }
    public void setRole(Role role){
        this.role = role;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public Boolean getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
    /*
    public List<BankAccount> getBankAccounts() {
        return bankAccounts;
    }
    public void setBankAccounts(List<BankAccount> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

     */

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }



    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", Tel='" + Tel + '\'' +
                ", Birth_Date=" + Birth_Date +
                ", role=" + role +
                ", password='" + password + '\'' +
                ", token='" + token + '\'' +
                ", mustChangePassword=" + mustChangePassword +
                ", cin='" + cin + '\'' +
                '}';
    }
}