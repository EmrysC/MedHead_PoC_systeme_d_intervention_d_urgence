package MeadHead.Poc.Gestion_position_trajet;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PositionGPS {
    private double latitude;
    private double longitude;

    @Override
    public String toString() {

        return this.getLatitude() + "," + this.getLongitude(); 

    }

}
