package MeadHead.Poc.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;

import MeadHead.Poc.service.UserService;
import MeadHead.Poc.dto.UserLoginDTO;
import MeadHead.Poc.securite.JwtService;
import MeadHead.Poc.dto.UserCreationDTO;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "user")
@Tag(name = "Gestion des Utilisateurs", description = "API pour l'authentification et la création de comptes utilisateurs.")
public class UserControler {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private JwtService jwtService;

    // @formatter:off
    @Operation(summary = "Authentification de l'utilisateur",
        description = "Connecte l'utilisateur en vérifiant les identifiants et retourne un jeton JWT.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Les identifiants de connexion (e-mail et mot de passe).",
        required = true,
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = UserLoginDTO.class))),
        responses = {
            
            // 200 SUCCESS
            @ApiResponse(responseCode="200",description="Connexion réussie, jeton JWT retourné",content=@Content(mediaType=MediaType.APPLICATION_JSON_VALUE,schema=@Schema(type="object",example="{\"Bearer\": \"eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9VU0VSIiwiZW1haWwiOiJlbXJ5c0BnZ2dtYWlsLmNvbSIsIm5vbSI6IkVtcnlzIiwicHJlbm9tIjoiQ2FsbGFpdCJ9.M6EGcvtDDw6zLUNSA9DwBRfZYe-q6vxyVAz9-27XPx4\"}"))),
            
            // 400 BAD REQUEST (Erreur de validation du DTO)
            @ApiResponse(responseCode="400",description="Erreur de validation des champs (DTO non conforme).",content=@Content(mediaType=MediaType.APPLICATION_JSON_VALUE,schema=@Schema(example="{\"timestamp\": \"2025-12-13T15:41:19.8602725\", \"message\": \"Erreur de validation des arguments\", \"errors\": { \"password\": \"Le mot de passe ne peut pas être vide.\" }, \"path\": \"uri=/api/user/connection\"}"))),
            
            // 401 UNAUTHORIZED (Cas 1: Utilisateur non trouvé)
            @ApiResponse(responseCode="401",description="Identifiants invalides (utilisateur non trouvé)",content=@Content(mediaType=MediaType.APPLICATION_JSON_VALUE,schema=@Schema(example="{\"timestamp\": \"2025-12-12T17:18:33.8377472\", \"message\": \"Échec\", \"errors\": {\"UsernameNotFoundException\": \"Utilisateur non trouvé avec l'e-mail : nouvel.utilisateeeeer@domaine.com\"}, \"path\": \"/api/user/connection\"}"))),
            
            // 401 UNAUTHORIZED (Cas 2: Mot de passe incorrect)
            @ApiResponse(responseCode="401",description="Mot de passe incorrect",content=@Content(mediaType=MediaType.APPLICATION_JSON_VALUE,schema=@Schema(example="{\"timestamp\": \"2025-12-13T15:36:52.2074054\", \"message\": \"Échec\", \"errors\": { \"BadCredentialsException\": \"Bad credentials\" }, \"path\": \"uri=/api/user/connection\"}")))
        })
// @formatter:on
    @PostMapping(path = "connection")
    public Map<String, String> connection(@Valid @RequestBody UserLoginDTO loginRequestDto) {

        final Authentication authenticate = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authenticate);

        Map<String, String> tokenMap = jwtService.generateToken(loginRequestDto.getEmail());

        return tokenMap;
    }

    // @formatter:off
    @Operation(
        summary = "Création d'un nouvel utilisateur",
        description = "Enregistre un nouvel utilisateur avec les informations fournies.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Informations de l'utilisateur à créer.",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserCreationDTO.class))),
        responses = {
            
            // 200 SUCCESS (Utilisateur créé)
            @ApiResponse(responseCode="200",description="Utilisateur créé avec succès (aucune donnée retournée)."),

            // 400 - Type 1: Erreur de Contrainte (ex: Mot de passe faible, Email invalide)
            @ApiResponse(responseCode="400",description="Erreur de contrainte : Mot de passe trop court ou faible, Email invalide, etc.",content=@Content(mediaType=MediaType.APPLICATION_JSON_VALUE,schema=@Schema(example="{\"timestamp\": \"2025-12-13T15:51:52.2186738\", \"message\": \"Erreur de validation des arguments\", \"errors\": { \"password\": \"Le mot de passe doit contenir au moins 8 caractères, dont une majuscule, une minuscule, un chiffre et un caractère spécial.\" }, \"path\": \"uri=/api/user/creation\"}"))),
            
            // 400 - Type 2: Erreur de Champ Manquant/Vide (ex: NotBlank/NotNull)
            @ApiResponse(responseCode="400",description="Erreur de validation : Champ manquant ou vide.",content=@Content(mediaType=MediaType.APPLICATION_JSON_VALUE,schema=@Schema(example="{\"timestamp\": \"2025-12-13T15:48:41.9635278\", \"message\": \"Erreur de validation des arguments\", \"errors\": { \"nom\": \"Le nom de passe ne peut pas être vide.\" }, \"path\": \"uri=/api/user/creation\"}"))),
            
            // 409 CONFLICT (Utilisateur déjà existant)
            @ApiResponse(responseCode="409",description="Conflit : L'adresse e-mail est déjà utilisée.",content=@Content(mediaType=MediaType.APPLICATION_JSON_VALUE,schema=@Schema(example="{\"timestamp\": \"2025-12-12T15:46:02.5872755\", \"message\": \"Conflit de données\", \"errors\": { \"email\": \"L'adresse e-mail 'utilisateur.deja@existant.com' est déjà utilisée par un autre compte.\" }, \"path\": \"uri=/api/user/creation\"}")))
        })
    // @formatter:on
    @PostMapping(path = "creation")
    public void creation(@Valid @RequestBody UserCreationDTO userDto) {

        userService.createUser(userDto);

    }

}
