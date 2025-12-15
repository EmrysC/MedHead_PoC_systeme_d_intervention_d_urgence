package MeadHead.Poc.dto;

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

        if (this.address != null && !this.address.trim().isEmpty()) {
            return this.address;
        }

        if (this.latitude != null && this.longitude != null) {
            return this.getLatitude() + "," + this.getLongitude();
        }

        return null;
    }
}
