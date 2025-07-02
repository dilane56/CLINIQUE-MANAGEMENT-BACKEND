package org.kfokam48.cliniquemanagementbackend.config;


import org.kfokam48.cliniquemanagementbackend.service.auth.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {



    private final JwtRequestFillter jwtRequestFilter;

    public SecurityConfig(JwtRequestFillter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // Utilisation de la nouvelle API pour d
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/secretaire/create",
                                "/api/patient/create",
                                "/api/administrateur/create",
                                "/api/medecin/create",
                                "/api/utilisateurs/all"
                        ).permitAll()

//                        // Restreindre l'accès aux endpoints spécifiques
//                        .requestMatchers("/api/medecins/**").hasRole("MEDECIN") // Accès uniquement pour les médecins
//                        .requestMatchers("/api/secretaire/**").hasRole("SECRETAIRE") // Accès uniquement pour les secrétaires
//                        .requestMatchers("/api/utilisateurs/**").hasRole("ADMIN") // Accès uniquement pour les administrateurs
//                        .requestMatchers("/api/**").hasRole("ADMIN") // Accès uniquement pour les administrateurs

                        // Restreindre l'accès à tous les autres endpoints
                        .anyRequest().authenticated())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpStatus.FORBIDDEN.value(), "Accès refusé : " + accessDeniedException.getMessage());
                        })
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);


        //  .headers(headers -> headers.frameOptions().disable());//uniquement lors de l'utilisation d'un BD H2
        return http.build();
    }




    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }





}