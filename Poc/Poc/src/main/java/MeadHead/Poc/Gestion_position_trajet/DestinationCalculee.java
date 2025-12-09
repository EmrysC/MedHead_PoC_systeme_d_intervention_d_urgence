package MeadHead.Poc.Gestion_position_trajet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DestinationCalculee {
    
private PositionGPS destinationPosition;
private String destinationAdresse;
private long distanceMetres;
private long dureeSecondes;
private boolean trajetValide;

}
