package meadhead.poc.securite;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import meadhead.poc.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws IOException, ServletException {

        String token = null;
        String email = null;
        boolean isTokenExpired = true;

        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
            try {
                isTokenExpired = jwtService.isTokenExpired(token);
                email = jwtService.extractedEmail(token);
            } catch (Exception e) {
                System.out.println("JwtFilter: Erreur lors de l'extraction du token: " + e.getMessage());
            }
        }

        UserDetails userDetails = null;

        if (token != null
                && !isTokenExpired
                && email != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                userDetails = this.userService.loadUserByUsername(email);
            } catch (UsernameNotFoundException e) {
                System.out.println("JwtFilter: Utilisateur non trouvé : " + email);
            }
        }

        if (userDetails != null) {
            var authorities = userDetails.getAuthorities();

            // Sécurité renforcée : on n'authentifie que si l'utilisateur a des rôles
            if (authorities != null && !authorities.isEmpty()) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                System.out.println("JwtFilter: Authentification réussie pour : " + email);
            } else {
                System.out.println("JwtFilter: ÉCHEC: Aucune autorité (rôle) pour : " + email);
            }
        }

        filterChain.doFilter(request, response);
    }
}
