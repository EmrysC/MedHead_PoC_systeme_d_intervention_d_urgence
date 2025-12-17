package MeadHead.Poc.securite;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import MeadHead.Poc.entites.User;
import MeadHead.Poc.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

// test JWT https://www.jwt.io/
@RequiredArgsConstructor
@Service
public class JwtService {

    private final UserService userService;

    @Value("${server.jwt.expiration}")
    private long expirationTime;

    @Value("${server.jwt.secret-key}")
    private String JwtSecretKey;
    private Key signingKey;

    @PostConstruct // on charge la clé pour ne pas la reculculer à chaque appel
    public void init() {
        this.signingKey = generateSigningKey();
    }

    public Map<String, String> generateToken(String email) {

        User user = userService.loadUserByUsername(email);

        return this.generateToken(user);

    }

    private Map<String, String> generateToken(User user) {

        final Map<String, Object> claims = Map.of(
                "nom", user.getNom(),
                "prenom", user.getPrenom(),
                "email", user.getEmail(),
                "role", user.getRole());

        final Instant now = Instant.now();
        final Instant expirationInstant = now.plusMillis(expirationTime);

        final String bearer = Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationInstant))
                .subject(user.getEmail())
                .claims(claims)
                .signWith(this.signingKey)
                .compact();

        return Map.of("Bearer", bearer);
    }

    private Key generateSigningKey() {

        byte[] keyBytes = Base64.getUrlDecoder().decode(JwtSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);

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

        try {
            return Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) this.signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.security.SignatureException | io.jsonwebtoken.ExpiredJwtException e) {
            return null;
        } catch (JwtException e) {
            return null;
        }
    }

}
