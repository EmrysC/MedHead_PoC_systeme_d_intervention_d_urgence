package MeadHead.Poc.securite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import MeadHead.Poc.service.UserService;
import lombok.RequiredArgsConstructor;

/* https://youtu.be/awP1N0R9rx0?t=1911 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ConfugarationSecuriteApplication {

        private final JwtService jwtService;
        private final UserService userService;

        @Bean
        public JwtFilter jwtFilter() {
                return new JwtFilter(userService, jwtService);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
                return httpSecurity
                                .csrf(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests((requests) -> requests

                                                .requestMatchers(HttpMethod.POST, "/user/connection").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/user/creation").permitAll()

                                                .requestMatchers(HttpMethod.GET, "/unitesoins").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/unitesoins/{id}").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/unitesoins/recherche_lit_dispo").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/trajets/optimises").permitAll()

                                                .anyRequest().authenticated())

                                .sessionManagement(
                                                httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
                                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authenticationProvider(authenticationProvider(
                                                httpSecurity.getSharedObject(UserDetailsService.class),
                                                bCryptPasswordEncoder()))

                                .addFilterAt(
                                                jwtFilter(),
                                                SecurityContextHolderFilter.class)

                                .build();
        }

        @Bean
        public BCryptPasswordEncoder bCryptPasswordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder) {

                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder);

                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

}
