package com.mezen.users.security;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	AuthenticationManager authMgr;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.sessionManagement(session -> 
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(csrf -> csrf.disable())
			.cors(cors -> cors.configurationSource(new CorsConfigurationSource() {
				@Override
				public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
					CorsConfiguration cors = new CorsConfiguration();
					// Autorise le front Angular quel que soit le port / l'hôte
					// (localhost:4200, 127.0.0.1:4200, IP réseau, etc.)
					cors.setAllowedOriginPatterns(Collections.singletonList("*"));
					cors.setAllowedMethods(Collections.singletonList("*"));
					cors.setAllowedHeaders(Collections.singletonList("*"));
					cors.setExposedHeaders(Collections.singletonList("Authorization"));
					return cors;
				}
			}))
			.authorizeHttpRequests(requests -> requests
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/login", "/register", "/verifyEmail/**").permitAll()
				.requestMatchers("/all").hasAuthority("ADMIN")
				.anyRequest().authenticated())
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((request, response, authException) -> {
					response.setStatus(401);
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					response.setCharacterEncoding(StandardCharsets.UTF_8.name());
					response.getWriter().write(
						"{\"error\":\"Unauthorized\",\"message\":\"Authentification requise\"}");
				})
				.accessDeniedHandler((request, response, accessDeniedException) -> {
					response.setStatus(403);
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					response.setCharacterEncoding(StandardCharsets.UTF_8.name());
					response.getWriter().write(
						"{\"error\":\"Forbidden\",\"message\":\"Accès refusé : droits insuffisants\"}");
				}))
			.addFilterBefore(new JWTAuthenticationFilter(authMgr), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}

