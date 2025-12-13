package MeadHead.Poc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationDTO {

    @NotBlank(message = "L'email ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "L'email doit être au format valide.")
    @Schema(description = "L'adresse e-mail de l'utilisateur.", example = "nouveau.utilisateur@domaine.com")
    private String email;

    @NotBlank(message = "Le mot de passe ne peut pas être vide.")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,}$", message = "Le mot de passe doit contenir au moins 8 caractères, dont une majuscule, une minuscule, un chiffre et un caractère spécial.")
    @Schema(description = "Le mot de passe de l'utilisateur (1 majuscule, 1minuscule, 1 chiffre, 1 caractère spécial).", example = "MonPassSécurisé#2025")
    private String password;

    @NotBlank(message = "Le nom de passe ne peut pas être vide.")
    @Schema(description = "Le nom de famille de l'utilisateur.", example = "Dupont")
    private String nom;

    @NotBlank(message = "Le prenom de passe ne peut pas être vide.")
    @Schema(description = "Le prénom de l'utilisateur.", example = "Jean")
    private String prenom;
}