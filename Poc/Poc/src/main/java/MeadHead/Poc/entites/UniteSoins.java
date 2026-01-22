package MeadHead.Poc.entites;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

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

    @Version
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;

}
