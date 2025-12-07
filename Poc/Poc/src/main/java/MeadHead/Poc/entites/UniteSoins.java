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
import java.util.stream.Collectors;

@Entity
// La contrainte d'unicité (UNIQUE) est appliquée ici,
// car elle implique plusieurs colonnes.
@Table(
    name = "unite_soins", 
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Hopital getHopital() {
        return hopital;
    }

    public void setHopital(Hopital hopital) {
        this.hopital = hopital;
    }

    public Specialisation getSpecialisation() {
        return specialisation;
    }

    public void setSpecialisation(Specialisation specialisation) {
        this.specialisation = specialisation;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Integer getLitsDisponibles() {
        return litsDisponibles;
    }

    public void setLitsDisponibles(Integer litsDisponibles) {
        this.litsDisponibles = litsDisponibles;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

}