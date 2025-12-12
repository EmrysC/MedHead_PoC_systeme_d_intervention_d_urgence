package MeadHead.Poc.entites;

import java.util.List;

import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

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

    @JsonIgnore // pour ne pas avoir de récursivité
    @OneToMany(mappedBy = "groupeSpecialite", cascade = CascadeType.ALL)
    private List<Specialisation> specialisations;

}
