package MeadHead.Poc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialisationOptionAdresseDTO {

    @NotNull
    @Schema(description = "Identifiant unique de l'option GPS (lecture seule).", example = "1")
    private Long id;

    @NotBlank(message = "L'adresse ne peut pas être vide.")
    @Size(min = 5, max = 255, message = "L'adresse doit contenir entre 5 et 255 caractères.")
    @Schema(description = "Adresse physique complète de la position.", example = "123 Rue de la Liberté, 38000 Grenoble")
    private String address;

}