package com.mzn.Stadium.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * Lit le header "Authorization: Bearer &lt;jwt&gt;", vérifie la signature et
 * place l'utilisateur ainsi que ses rôles dans le SecurityContext.
 *
 * Si aucun token n'est fourni, la requête continue sans authentification :
 * c'est SecurityConfig qui décide ensuite de renvoyer 401 ou d'autoriser
 * l'accès (ex. requêtes OPTIONS de préflight CORS).
 */
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// Les requêtes de préflight CORS ne portent jamais de token.
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}

		String header = request.getHeader("Authorization");

		if (header == null || !header.startsWith(SecParams.PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String jwt = header.substring(SecParams.PREFIX.length()).trim();
			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SecParams.SECRET)).build();
			DecodedJWT decodedJWT = verifier.verify(jwt);

			String username = decodedJWT.getSubject();
			List<String> roles = decodedJWT.getClaim("roles").asList(String.class);

			Collection<GrantedAuthority> authorities = new ArrayList<>();
			if (roles != null) {
				for (String role : roles) {
					if (role != null && !role.isBlank()) {
						authorities.add(new SimpleGrantedAuthority(role.trim()));
					}
				}
			}

			UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(username, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(authentication);

		} catch (JWTVerificationException | IllegalArgumentException e) {
			// Token invalide, altéré ou expiré : réponse JSON claire (401).
			SecurityContextHolder.clearContext();
			writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
					"Unauthorized", "Token invalide ou expiré");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private void writeJsonError(HttpServletResponse response, int status,
			String error, String message) throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(
				"{\"error\":\"" + error + "\",\"message\":\"" + message + "\"}");
	}
}
