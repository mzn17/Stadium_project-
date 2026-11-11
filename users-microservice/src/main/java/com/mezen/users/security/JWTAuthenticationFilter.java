package com.mezen.users.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private AuthenticationManager authenticationManager;

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
		super();
		this.authenticationManager = authenticationManager;
		setAuthenticationManager(authenticationManager);
		setFilterProcessesUrl("/login");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, 
			HttpServletResponse response) throws AuthenticationException {
		String username = null;
		String password = null;
		try {
			// On lit uniquement username / password : le front peut envoyer
			// d'autres champs (email, roles, enabled...) sans faire échouer le login.
			JsonNode json = new ObjectMapper().readTree(request.getInputStream());
			if (json != null) {
				username = json.hasNonNull("username") ? json.get("username").asText() : null;
				password = json.hasNonNull("password") ? json.get("password").asText() : null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (username == null || password == null) {
			throw new org.springframework.security.authentication.BadCredentialsException(
					"Nom d'utilisateur ou mot de passe manquant");
		}

		return authenticationManager.authenticate(new
				UsernamePasswordAuthenticationToken(username, password));
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, 
			HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		org.springframework.security.core.userdetails.User springUser = 
				(org.springframework.security.core.userdetails.User) authResult.getPrincipal();

		List<String> roles = new ArrayList<>();
		springUser.getAuthorities().forEach(au -> {
			roles.add(au.getAuthority());
		});

		String jwt = JWT.create()
				.withSubject(springUser.getUsername())
				.withArrayClaim("roles", roles.toArray(new String[roles.size()]))
				.withExpiresAt(new Date(System.currentTimeMillis() + SecParams.EXP_TIME))
				.sign(Algorithm.HMAC256(SecParams.SECRET));

		// Le front ajoute lui-même le préfixe "Bearer "
		response.addHeader("Authorization", jwt);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException failed)
			throws IOException, ServletException {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		// Compte non activé : le front affiche un message spécifique.
		if (failed instanceof DisabledException) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().write(
					"{\"errorCause\":\"disabled\",\"message\":\"L'utilisateur est désactivé !\"}");
			return;
		}

		// Évite la redirection HTML par défaut vers /login?error :
		// le front Angular reçoit un vrai 401 avec un message JSON.
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().write(
				"{\"error\":\"Unauthorized\",\"message\":\"login ou mot de passe erronés\"}");
	}
}

