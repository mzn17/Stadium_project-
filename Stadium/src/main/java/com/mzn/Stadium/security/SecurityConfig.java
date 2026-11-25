package com.mzn.Stadium.security;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration de sécurité du service Stadium.
 *
 * Règles métier (identiques à ce que le frontend affiche) :
 *   • GET    → tout utilisateur authentifié (rôles ADMIN ou USER)
 *   • POST / PUT / PATCH / DELETE → ADMIN uniquement
 *
 * Le token est le même que celui émis par users-microservice : aucun appel
 * réseau entre les deux backends n'est nécessaire pour le vérifier.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.sessionManagement(session ->
					session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
					// Préflight CORS et page d'erreur interne
					.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
					.requestMatchers("/error").permitAll()

					// Lecture : accessible aux utilisateurs connectés
					.requestMatchers(HttpMethod.GET, "/**").hasAnyAuthority("ADMIN", "USER")

					// Écriture : réservée à l'administrateur
					.requestMatchers(HttpMethod.POST, "/**").hasAuthority("ADMIN")
					.requestMatchers(HttpMethod.PUT, "/**").hasAuthority("ADMIN")
					.requestMatchers(HttpMethod.PATCH, "/**").hasAuthority("ADMIN")
					.requestMatchers(HttpMethod.DELETE, "/**").hasAuthority("ADMIN")

					.anyRequest().authenticated())
			.exceptionHandling(ex -> ex
					.authenticationEntryPoint((request, response, authException) ->
							writeJsonError(response, 401, "Unauthorized",
									"Authentification requise"))
					.accessDeniedHandler((request, response, accessDeniedException) ->
							writeJsonError(response, 403, "Forbidden",
									"Accès refusé : droits insuffisants")))
			.addFilterBefore(new JwtAuthorizationFilter(),
					UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// Autorise le front Angular quel que soit l'hôte / le port.
		configuration.setAllowedOriginPatterns(List.of("*"));
		configuration.setAllowedMethods(
				List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		// Permet à Angular de lire le JWT lors du login (users-microservice),
		// et reste utile si un endpoint renvoie un header Authorization.
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	private void writeJsonError(jakarta.servlet.http.HttpServletResponse response,
			int status, String error, String message) throws java.io.IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(
				"{\"error\":\"" + error + "\",\"message\":\"" + message + "\"}");
	}
}
