package MeadHead.Poc.Gestion_position_trajet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DestinationCalculeeDTO {

    private PositionGPS destinationPosition;
    private String destinationAdresse;
    private long distanceMetres;
    private long dureeMinutes;
    private boolean trajetValide;

}
