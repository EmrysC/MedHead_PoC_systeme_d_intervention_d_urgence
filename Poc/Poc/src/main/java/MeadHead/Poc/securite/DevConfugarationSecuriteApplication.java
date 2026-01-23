package MeadHead.Poc.securite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import MeadHead.Poc.service.UserService;
import MeadHead.Poc.utils.Untested_dev_Generated;

@Configuration
@EnableWebSecurity
@Profile("dev")
@Untested_dev_Generated
public class DevConfugarationSecuriteApplication {

    private final JwtService jwtService;
    private final UserService userService;

    public DevConfugarationSecuriteApplication(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(userService, jwtService);
    }

    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity httpSecurity, AuthenticationProvider authenticationProvider,
            PasswordEncoder passwordEncoder,
            JwtFilter jwtFilter) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                // Pages accessibles sans authentification
                .requestMatchers("/login").permitAll()
                // --- ROUTES PUBLIQUES ---
                .requestMatchers(HttpMethod.POST, "/user/connection",
                        "/user/creation").permitAll()
                // --- SWAGGER & DOCS ---
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/v2/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/webjars/**").permitAll()
                // pages protégées
                .requestMatchers("/dashboard",
                        "search-hospital").permitAll()
                // --- ROUTES PROTÉGÉES ---
                .requestMatchers(HttpMethod.GET,
                        "/unitesoins/**",
                        "/specilites",
                        "/actuator/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/reservation/lit").authenticated()
                // --- VERROUILLAGE FINAL ---
                .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
