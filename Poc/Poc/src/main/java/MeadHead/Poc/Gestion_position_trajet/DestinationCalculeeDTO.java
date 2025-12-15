package MeadHead.Poc.Gestion_position_trajet;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DestinationCalculeeDTO {

    private PositionGPS destinationPosition;
    private String destinationAdresse;
    private long distanceMetres;
    private long dureeSecondes;
    private boolean trajetValide;

}
