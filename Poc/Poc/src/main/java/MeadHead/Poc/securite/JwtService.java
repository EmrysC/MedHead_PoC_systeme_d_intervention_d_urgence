package MeadHead.Poc.securite;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Map;
import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;

import MeadHead.Poc.entites.User;
import MeadHead.Poc.service.UserService;


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
                "role", user.getAuthorities().iterator().next().getAuthority()); 

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

    return Keys.hmacShaKeyFor(JwtSecretKey.getBytes(StandardCharsets.UTF_8));
}

    public String extractedEmail(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenExpired(String token) {
        final Date expiration = extractClaim(token, Claims::getExpiration);

        if (expiration == null) {
            return true;
        }

        return expiration.before(new Date());
    }

public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    
    if (claims == null) { 
        return null; 
    }
    
    return claimsResolver.apply(claims);
}

    private Claims extractAllClaims(String token) {

        System.out.println("JwtSecretKey" + JwtSecretKey);

        try {
            return Jwts.parser()
                    .setSigningKey(getJwtSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {

            System.err.println("Erreur de validation/parsing JWT : " + e.getMessage());
            return null;
        }
    }


}
