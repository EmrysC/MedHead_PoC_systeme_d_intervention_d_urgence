package MeadHead.Poc.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "specialisation")
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


}