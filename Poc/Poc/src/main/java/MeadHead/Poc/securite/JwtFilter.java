package MeadHead.Poc.securite;

import java.util.Collection;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import MeadHead.Poc.service.UserService;
import jakarta.servlet.FilterChain;
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
            throws java.io.IOException, jakarta.servlet.ServletException {

        String token = null;
        String email = null;
        boolean isTokenExpired = true;

        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
            isTokenExpired = jwtService.isTokenExpired(token);
            email = jwtService.extractedEmail(token);
        }

        UserDetails userDetails = null;

        if (token != null
                && !isTokenExpired
                && email != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                // Chargement des détails de l'utilisateur
                userDetails = this.userService.loadUserByUsername(email);
            } catch (UsernameNotFoundException e) {
                // L'utilisateur n'existe pas ou l'e-mail était incorrect, le filtre continue
                // sans authentification, menant au 403 final par Spring Security.
                System.out.println("JwtFilter: ERREUR: Utilisateur non trouvé pour l'email: " + email);
            }
        }

        if (userDetails != null) {

            // Vérification de robustesse des autorités (déjà présente dans votre code)
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            // S'assurer que les autorités ne sont pas nulles/vides pour l'état authentifié
            if (authorities != null && !authorities.isEmpty()) {

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities);

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);

    }
}
