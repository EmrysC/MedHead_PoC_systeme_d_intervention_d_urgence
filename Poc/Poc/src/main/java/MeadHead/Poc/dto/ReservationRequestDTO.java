package MeadHead.Poc.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDTO {

    @NotNull(message = "L'identifiant de l'unité de soins ne peut pas être nul.")
    @Positive(message = "L'identifiant de l'unité de soins doit être un entier positif.")
    @Digits(integer = 10, fraction = 0, message = "L'identifiant doit être un nombre entier sans décimales.")
    private Long uniteSoinsId;

}
