package MeadHead.Poc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@OneOfAddressOrGps
public class SpecialisationTrajetDTO {

    @NotNull
    @Schema(description = "Identifiant unique de l'option de spécialisation", example = "1")
    private Long specialisationId;

    @DecimalMin(value = "-90.0", message = "La latitude doit être supérieure ou égale à -90.")
    @DecimalMax(value = "90.0", message = "La latitude doit être inférieure ou égale à 90.")
    @Digits(integer = 2, fraction = 8, message = "Format GPS invalide : trop de chiffres.")
    @Schema(description = "Coordonnée de latitude.", example = "45.1885")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "La longitude doit être supérieure ou égale à -180.")
    @DecimalMax(value = "180.0", message = "La longitude doit être inférieure ou égale à 180.")
    @Digits(integer = 3, fraction = 8, message = "Format GPS invalide : trop de chiffres.")
    @Schema(description = "Coordonnée de longitude.", example = "5.7245")
    private Double longitude;

    @Size(min = 5, max = 255, message = "L'adresse doit contenir entre 5 et 255 caractères.")
    @Schema(description = "Adresse", example = "1 Av. Gustave Eiffel, 75007 Paris")
    private String adresse;

}
