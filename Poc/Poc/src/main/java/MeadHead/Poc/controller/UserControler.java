package MeadHead.Poc.controller;

import MeadHead.Poc.service.UserService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import MeadHead.Poc.dto.LoginRequestDto;
import MeadHead.Poc.dto.UserCreationDto;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "user")

public class UserControler {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping(path = "connection")
    public void connection(@RequestBody LoginRequestDto loginRequestDto) {

        log.info("Tentative de connexion pour l'utilisateur: " + loginRequestDto.getEmail());

        // CRÉATION DU JETON D'AUTHENTIFICATION (Email & Mot de passe en clair)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()));

        log.info(loginRequestDto.getEmail() + " " + authentication.isAuthenticated());

        // STOCKAGE DU JETON DANS LE CONTEXTE DE SÉCURITÉ
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("Utilisateur " + loginRequestDto.getEmail() + " authentifié avec succès.");

    }

    @PostMapping(path = "creation")
    public void creation(@RequestBody UserCreationDto userDto) {

        log.info("Tentative de creation pour l'utilisateur: " + userDto.getEmail());
        
        userService.createUser(userDto);
        
        log.info("Création réussie pour l'utilisateur: " + userDto.getEmail());

    }

}
