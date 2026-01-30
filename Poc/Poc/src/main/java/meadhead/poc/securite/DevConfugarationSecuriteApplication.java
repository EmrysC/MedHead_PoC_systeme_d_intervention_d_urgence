package meadhead.poc.securite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import meadhead.poc.service.UserService;
import meadhead.poc.utils.Untested_dev_Generated;

@Configuration
@EnableWebSecurity
@Profile("dev")
@Untested_dev_Generated
public class DevConfugarationSecuriteApplication {

    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public DevConfugarationSecuriteApplication(JwtService jwtService, UserService userService, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(userService, jwtService);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder
                = http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(userService)
                .passwordEncoder(passwordEncoder);

        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity httpSecurity, AuthenticationProvider authenticationProvider,
            PasswordEncoder passwordEncoder,
            JwtFilter jwtFilter) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests
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

}
