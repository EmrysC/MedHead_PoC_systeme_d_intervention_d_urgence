package meadhead.poc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PositionDTO {

    private String address;
    private Double latitude;
    private Double longitude;

    public PositionDTO(SpecialisationTrajetDTO dto) {
        this.address = dto.getAdresse();
        this.latitude = dto.getLatitude();
        this.longitude = dto.getLongitude();
    }

    public String getPositionValid() {

        if (adresseIsValid()) {
            return this.address;
        }

        if (positionIsValid()) {
            return this.getLatitude() + "," + this.getLongitude();
        }

        return null;
    }

    public boolean onlyAdresseIsValid() {
        return adresseIsValid() && !positionIsValid();
    }

    private boolean adresseIsValid() {
        return this.address != null && !this.address.trim().isEmpty();
    }

    private boolean positionIsValid() {
        return this.latitude != null && this.longitude != null;
    }
}
