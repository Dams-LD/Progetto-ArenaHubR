package com.arenaHub.backendArena.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Configurazione della sicurezza.
 *
 * Il login lo fa Supabase Auth dal frontend: qui ci limitiamo a verificare
 * il token JWT che Angular allega a ogni chiamata nell'header Authorization.
 * Gli indirizzi per la verifica stanno in application.properties
 * (issuer-uri e jwk-set-uri).
 *
 * Il ruolo UTENTE/ADMIN NON si controlla qui: il token di Supabase contiene
 * un claim "role" che vale sempre "authenticated" e non c'entra con il nostro
 * ruolo applicativo. Il ruolo vero sta nella tabella profiles e lo leggiamo
 * nei service.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /** Da dove arriva il frontend in sviluppo. */
    private static final String ORIGINE_FRONTEND = "http://localhost:4200";

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // CORS: senza questo Angular riceve errore su ogni chiamata
            .cors(Customizer.withDefaults())

            // CSRF non serve: non usiamo cookie di sessione, ma un token
            .csrf(csrf -> csrf.disable())

            // Nessuna sessione lato server: ogni richiesta porta il suo token
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                // Il browser manda una OPTIONS prima delle chiamate vere
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Endpoint pubblici del contratto: catalogo, dettaglio,
                // categorie e calendario si vedono senza login
                .requestMatchers(HttpMethod.GET, "/api/tornei").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tornei/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categorie").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/calendario").permitAll()

                // Tutto il resto richiede un token valido.
                // Chi può fare cosa fra UTENTE e ADMIN lo decidono i service
                // leggendo profiles.ruolo.
                .anyRequest().authenticated()
            )

            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))

            // Senza queste due righe Spring Security risponde con il suo
            // formato di errore, che non è quello promesso dal contratto
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) ->
                    scriviErrore(res, HttpServletResponse.SC_UNAUTHORIZED,
                                 "Devi effettuare il login"))
                .accessDeniedHandler((req, res, e) ->
                    scriviErrore(res, HttpServletResponse.SC_FORBIDDEN,
                                 "Operazione non consentita"))
            );

        return http.build();
    }

    /** Scrive a mano la risposta nel formato {"errore": "..."} del contratto. */
    private void scriviErrore(HttpServletResponse response, int stato, String messaggio)
            throws java.io.IOException {
        response.setStatus(stato);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"errore\":\"" + messaggio + "\"}");
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(ORIGINE_FRONTEND));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // false perché il token viaggia in un header, non in un cookie.
        
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}