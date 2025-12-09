package MeadHead.Poc.entites;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "groupe_specialite")
public class GroupeSpecialite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   
    private String nom;


}
