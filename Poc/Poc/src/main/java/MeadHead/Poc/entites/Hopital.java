package MeadHead.Poc.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Hopital")
public class Hopital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 

    @Column(name = "nom", nullable = false, length = 255)
    private String nom;


    public Hopital() {
    }

    public Hopital(String nom) {
        this.nom = nom;
    }

}