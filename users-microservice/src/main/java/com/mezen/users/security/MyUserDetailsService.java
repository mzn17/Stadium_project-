package com.mezen.users.security;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.mezen.users.entities.User;
import com.mezen.users.service.UserService;

@Service
public class MyUserDetailsService implements UserDetailsService {
	@Autowired
	UserService userService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userService.findUserByUsername(username);
		if (user == null) 
			throw new UsernameNotFoundException("Utilisateur introuvable !");

		List<GrantedAuthority> auths = new ArrayList<>();
		if (user.getRoles() != null) {
			user.getRoles().forEach(role -> {
				GrantedAuthority auhority = new SimpleGrantedAuthority(role.getRole());
				auths.add(auhority);
			});
		}

		// "enabled" peut être null dans d'anciennes lignes de la base :
		// on le considère comme activé pour ne pas bloquer le login.
		boolean enabled = user.getEnabled() == null || user.getEnabled();

		return new org.springframework.security.core.userdetails.User(
				user.getUsername(), user.getPassword(), enabled, true, true, true, auths);
	}
}

