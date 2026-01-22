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
                .requestMatchers(HttpMethod.POST,
                        "/api/user/connection",
                        "/api/user/creation")
                .permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/api/unitesoins",
                        "/api/unitesoins/{id}",
                        "/api/unitesoins/recherche_lit_dispo",
                        "/api/unitesoins/trajets_optimises",
                        "/api/unitesoins/trajets")
                .authenticated()
                .requestMatchers(HttpMethod.POST, "/api/reservation/lit").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/specilites").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/actuator").authenticated()
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/v2/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/webjars/**")
                .permitAll()
                .anyRequest().permitAll())
                .sessionManagement(
                        httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)
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
