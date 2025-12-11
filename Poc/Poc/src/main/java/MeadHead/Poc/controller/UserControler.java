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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import MeadHead.Poc.service.UserService;
import MeadHead.Poc.dto.UserLoginDTO;
import MeadHead.Poc.securite.JwtService;
import MeadHead.Poc.dto.UserCreationDTO;


@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "user")

public class UserControler {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private JwtService jwtService;

    @PostMapping(path = "connection")

    public Map<String, String> connection(@RequestBody UserLoginDTO loginRequestDto) {

        log.info("Tentative de connexion pour l'utilisateur: " + loginRequestDto.getEmail());


        final Authentication authenticate = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authenticate);

        Map<String, String> tokenMap = jwtService.generateToken(loginRequestDto.getEmail());

        log.info("Résultat Authentifié: " + authenticate.isAuthenticated()); // Log de confirmation

        return tokenMap;
    }

    @PostMapping(path = "creation")
    public void creation(@RequestBody UserCreationDTO userDto) {

        log.info("Tentative de creation pour l'utilisateur: " + userDto.getEmail());

        userService.createUser(userDto);

        log.info("Création réussie pour l'utilisateur: " + userDto.getEmail());

    }

}
