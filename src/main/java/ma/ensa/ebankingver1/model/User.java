package ma.ensa.ebankingver1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDate;
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

    @Column(name = "token")
    private String token;

    @Column(name = "must_change_password")
    private Boolean mustChangePassword;
    @Column(unique = true, nullable = false)
    private String cin;

    // Stocker seulement le nom du fichier (pas le chemin complet)
    //@Column(name = "cin_photo_filename")
    //private String cinPhotoFilename;

    // Getters et Setters
    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    //public String getCinPhotoFilename() { return cinPhotoFilename; }
    //public void setCinPhotoFilename(String cinPhotoFilename) { this.cinPhotoFilename = cinPhotoFilename; }

    // Méthode helper pour obtenir l'URL de l'image
    //public String getCinPhotoUrl() {
    //  if (cinPhotoFilename != null) {
    //    return "/api/employees/cin-photo/" + cinPhotoFilename;}
    //return null;}


    @OneToMany(mappedBy = "client")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<BankAccount> bankAccounts;


    public User() {
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
    public List<BankAccount> getBankAccounts() {
        return bankAccounts;
    }
    public void setBankAccounts(List<BankAccount> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

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