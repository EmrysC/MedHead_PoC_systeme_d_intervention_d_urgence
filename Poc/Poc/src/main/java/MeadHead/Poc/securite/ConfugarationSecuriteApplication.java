package MeadHead.Poc.securite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import lombok.RequiredArgsConstructor;

import MeadHead.Poc.service.UserService;

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

                                                .requestMatchers(HttpMethod.POST,
                                                                "/user/connection",
                                                                "/user/creation")
                                                .permitAll()

                                                .requestMatchers(HttpMethod.GET,
                                                                "/unitesoins",
                                                                "/unitesoins/{id}",
                                                                "/unitesoins/recherche_lit_dispo",
                                                                "/unitesoins/trajets_optimises")
                                                .permitAll()

                                                .requestMatchers(HttpMethod.POST, "/reservation/lit").permitAll()

                                                .requestMatchers(HttpMethod.GET, "/specilites").permitAll()

                                                .requestMatchers(HttpMethod.GET, "/actuator").permitAll()

                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/v2/api-docs/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/swagger-resources/**",
                                                                "/webjars/**")
                                                .permitAll()

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
