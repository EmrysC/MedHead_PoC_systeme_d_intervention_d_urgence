package meadhead.poc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialisationOptionGPSDTO {

    @NotNull
    @Schema(description = "Identifiant unique de l'option de spécialisation", example = "1")
    private Long id;

    // La latitude doit être entre -90 et 90
    @NotNull(message = "La latitude est requise.")
    @DecimalMin(value = "-90.0", message = "La latitude doit être supérieure ou égale à -90.")
    @DecimalMax(value = "90.0", message = "La latitude doit être inférieure ou égale à 90.")
    @Schema(description = "Coordonnée de latitude.", example = "45.1885")
    private Double latitude;

    // La longitude doit être entre -180 et 180
    @NotNull(message = "La longitude est requise.")
    @DecimalMin(value = "-180.0", message = "La longitude doit être supérieure ou égale à -180.")
    @DecimalMax(value = "180.0", message = "La longitude doit être inférieure ou égale à 180.")
    @Schema(description = "Coordonnée de longitude.", example = "5.7245")
    private Double longitude;

}
