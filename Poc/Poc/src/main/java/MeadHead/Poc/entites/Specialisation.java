package MeadHead.Poc.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Specialisation")
public class Specialisation {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(name = "nom", nullable = false, unique = true, length = 150)
    private String nom;

    @ManyToOne(optional = false) 
    @JoinColumn(
        name = "groupe_specialite_id", 
        referencedColumnName = "id" 
    )
    private GroupeSpecialite groupeSpecialite; 

    // Constructeurs
    public Specialisation() {
    }

    public Specialisation(String nom, GroupeSpecialite groupeSpecialite) {
        this.nom = nom;
        this.groupeSpecialite = groupeSpecialite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public GroupeSpecialite getGroupeSpecialite() {
        return groupeSpecialite;
    }

    public void setGroupeSpecialite(GroupeSpecialite groupeSpecialite) {
        this.groupeSpecialite = groupeSpecialite;
    }

}