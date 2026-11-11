package com.mezen.users.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

public class JWTAuthorizationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, 
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getRequestURI().substring(request.getContextPath().length());

		// Les endpoints publics ne nécessitent pas de token
		if (path.equals("/login") || path.startsWith("/register")
				|| path.startsWith("/verifyEmail")
				|| "OPTIONS".equalsIgnoreCase(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}

		String jwt = request.getHeader("Authorization");

		if (jwt == null || !jwt.startsWith(SecParams.PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SecParams.SECRET)).build();
			// enlever le préfixe Bearer du jwt
			jwt = jwt.substring(SecParams.PREFIX.length());

			DecodedJWT decodedJWT = verifier.verify(jwt);
			String username = decodedJWT.getSubject();
			List<String> roles = decodedJWT.getClaim("roles").asList(String.class);

			Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
			if (roles != null) {
				for (String r : roles)
					authorities.add(new SimpleGrantedAuthority(r));
			}

			UsernamePasswordAuthenticationToken user =
					new UsernamePasswordAuthenticationToken(username, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(user);
		} catch (JWTVerificationException e) {
			// Token invalide ou expiré : on renvoie 401 (et non une erreur 500)
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.getWriter().write(
					"{\"error\":\"Unauthorized\",\"message\":\"Token invalide ou expiré\"}");
			return;
		}

		filterChain.doFilter(request, response);
	}
}

