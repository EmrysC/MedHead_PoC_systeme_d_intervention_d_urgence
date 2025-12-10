package MeadHead.Poc.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long uniteSoinsId;

    private String utilisateurEmail;

    private LocalDateTime dateReservation;
    
    public Reservation(Long uniteSoinsId, String utilisateurEmail) {
        this.uniteSoinsId = uniteSoinsId;
        this.utilisateurEmail = utilisateurEmail;
        this.dateReservation = LocalDateTime.now(); 
    }
}