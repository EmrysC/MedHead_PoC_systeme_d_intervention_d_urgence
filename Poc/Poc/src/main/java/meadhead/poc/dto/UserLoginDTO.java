package meadhead.poc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {

    @NotBlank(message = "L'email ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "L'email doit être au format valide.")
    @Schema(description = "L'adresse e-mail de l'utilisateur.", example = "nouveau.utilisateur@domaine.com")
    private String email;

    @NotBlank(message = "Le mot de passe ne peut pas être vide.")
    @Size(min = 8, max = 255, message = "Le mot de passe doit contenir au moins 8 caractères.")
    @Schema(description = "Le mot de passe de l'utilisateur (1 majuscule, 1minuscule, 1 chiffre, 1 caractère spécial).", example = "MonPassSécurisé#2025")
    private String password;
}
