package MeadHead.Poc.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal; // Utilisé pour le type NUMERIC/DECIMAL

@Entity
// La contrainte d'unicité (UNIQUE) est appliquée ici,
// car elle implique plusieurs colonnes.
@Table(
    name = "UniteSoins", 
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"hopital_id", "specialisation_id", "latitude", "longitude"}
    )
)
public class UniteSoins {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hopital_id") 
    private Hopital hopital;

    @ManyToOne(optional = false)
    @JoinColumn(name = "specialisation_id")
    private Specialisation specialisation;

    @Column(name = "adresse", length = 512)
    private String adresse;

    @Column(name = "lits_disponibles", nullable = false)
    private Integer litsDisponibles;

    @Column(name = "latitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal longitude;

    // Constructeur 
    public UniteSoins() {
    }

    public UniteSoins(Hopital hopital, Specialisation specialisation, String adresse, Integer litsDisponibles, BigDecimal latitude, BigDecimal longitude) {
        this.hopital = hopital;
        this.specialisation = specialisation;
        this.adresse = adresse;
        this.litsDisponibles = litsDisponibles;
        this.latitude = latitude;
        this.longitude = longitude;
    }


}