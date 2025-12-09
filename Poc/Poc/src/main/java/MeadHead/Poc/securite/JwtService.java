package MeadHead.Poc.securite;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;


import io.jsonwebtoken.security.Keys;
import java.security.Key;

import MeadHead.Poc.entites.User;
import MeadHead.Poc.service.UserService;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Date;
import java.time.Instant;

@RequiredArgsConstructor
@Service
public class JwtService {

    private final UserService userService;

    @Value("${server.jwt.expiration}")
    private long expirationTime;

    @Value("${server.jwt.secret-key}")
    private String JwtSecretKey;

    public Map<String, String> generateToken(String email) {

        User user = userService.loadUserByUsername(email);

        return this.generateToken(user);

    }

    private Map<String, String> generateToken(User user) {

        final Map<String, Object> claims = Map.of(
                "nom", user.getNom(),
                "prenom", user.getPrenom(),
                "email", user.getEmail(),
                "roles", user.getAuthorities().toString());

        final Instant now = Instant.now();
        final Instant expirationInstant = now.plusMillis(expirationTime);

        final String bearer = Jwts.builder()
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expirationInstant))
                .setSubject(user.getEmail())
                .setClaims(claims)
                .signWith(getJwtSecretKey())
                .compact();

        return Map.of("Bearer", bearer);
    }

    private Key getJwtSecretKey() {

        return Keys.hmacShaKeyFor(JwtSecretKey.getBytes());
    }

}
