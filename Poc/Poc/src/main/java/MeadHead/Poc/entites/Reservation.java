package MeadHead.Poc.entites;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Clé étrangère vers l'unité de soins
    @ManyToOne
    @JoinColumn(name = "unite_soins_id", nullable = false)
    private UniteSoins uniteSoins;

    // Clé étrangère vers l'utilisateur
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime dateReservation;

    // Constructeur mis à jour pour accepter les objets
    public Reservation(UniteSoins uniteSoins, User user) {
        this.uniteSoins = uniteSoins;
        this.user = user;
        this.dateReservation = LocalDateTime.now();
    }
}
