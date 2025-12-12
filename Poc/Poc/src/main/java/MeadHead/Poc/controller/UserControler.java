package MeadHead.Poc.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Operation(summary = "Authentification de l'utilisateur", description = "Connecte l'utilisateur en vérifiant les identifiants et retourne un jeton JWT.", responses = {
            @ApiResponse(responseCode = "200", description = "Connexion réussie, jeton JWT retourné"),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides")
    })
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

    @PostMapping(path = "creation")
    public void creation(@Valid @RequestBody UserCreationDTO userDto) {

        log.info("Tentative de creation pour l'utilisateur: " + userDto.getEmail());

        userService.createUser(userDto);

        log.info("Création réussie pour l'utilisateur: " + userDto.getEmail());

    }

}
