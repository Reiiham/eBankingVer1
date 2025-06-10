package ma.ensa.ebankingver1.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "rib", nullable = false)
    private String rib;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation", nullable = false)
    private RelationType relation;


    @Column(name = "surnom") // Nom d'affichage personnalisé
    private String surnom;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "actif")
    private Boolean actif = true;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private BankAccount account;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        if (this.actif == null) {
            this.actif = true;
        }
    }

    // Constructeurs
    public Beneficiary() {}

    public Beneficiary(String nom, String prenom, String rib, RelationType relation, User user) {
        this.nom = nom;
        this.prenom = prenom;
        this.rib = rib;
        this.relation = relation;
        this.user = user;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }

    public RelationType getRelation() { return relation; }
    public void setRelation(RelationType relation) { this.relation = relation; }


    public String getSurnom() { return surnom; }
    public void setSurnom(String surnom) { this.surnom = surnom; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public BankAccount getAccount() { return account; }
    public void setAccount(BankAccount account) { this.account = account; }
    // Méthode utilitaire pour obtenir le nom complet
    public String getNomComplet() {
        if (prenom != null && !prenom.trim().isEmpty()) {
            return prenom + " " + nom;
        }
        return nom;
    }

    // Méthode pour obtenir le nom d'affichage (surnom ou nom complet)
    public String getNomAffichage() {
        return (surnom != null && !surnom.trim().isEmpty()) ? surnom : getNomComplet();
    }
}
