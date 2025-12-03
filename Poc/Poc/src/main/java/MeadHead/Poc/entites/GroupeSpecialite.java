package MeadHead.Poc.entites;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

@Entity
@Table(name = "GroupeSpecialite")
public class GroupeSpecialite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   
    private String nom;


    // Constructeurs

    public GroupeSpecialite() {
    }

    public GroupeSpecialite(String nom) {
        this.nom = nom;
    }



}
