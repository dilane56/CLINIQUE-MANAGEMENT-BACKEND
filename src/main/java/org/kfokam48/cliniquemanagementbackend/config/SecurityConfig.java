package org.kfokam48.cliniquemanagementbackend.config;


import org.kfokam48.cliniquemanagementbackend.service.auth.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.Arrays;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    @Value("${cors.allowed.origins:*}")
    private String allowedOrigins;
    
    @Value("${cors.allowed.methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;
    
    @Value("${cors.allowed.headers:*}")
    private String allowedHeaders;
    
    @Value("${cors.allow.credentials:true}")
    private boolean allowCredentials;

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
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors
                        .configurationSource(request -> {
                            var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                            corsConfig.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
                            corsConfig.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
                            corsConfig.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
                            corsConfig.setAllowCredentials(allowCredentials);
                            return corsConfig;
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/secretaires",
                                "/api/patient",
                                "/api/administrateurs/create",
                                "/api/medecins",
                                "/api/utilisateurs",
                                "/ws-chat/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpStatus.FORBIDDEN.value(), "Accès refusé : " + accessDeniedException.getMessage());
                        })
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }




    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


}