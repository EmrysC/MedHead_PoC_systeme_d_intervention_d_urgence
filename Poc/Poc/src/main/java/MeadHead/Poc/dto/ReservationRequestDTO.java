package MeadHead.Poc.dto;

import jakarta.validation.constraints.NotNull;
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
    private Long uniteSoinsId;

}
