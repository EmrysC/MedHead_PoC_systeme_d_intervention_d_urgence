package MeadHead.Poc.Gestion_position_trajet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
