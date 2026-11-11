package com.mezen.users.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mezen.users.entities.Role;
import com.mezen.users.entities.User;
import com.mezen.users.repos.RoleRepository;
import com.mezen.users.repos.UserRepository;

@Transactional
@Service
public class UserServiceImpl implements UserService {
	@Autowired
	UserRepository userRep;
	@Autowired
	RoleRepository roleRep;
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public User saveUser(User user) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		if (user.getEnabled() == null)
			user.setEnabled(true);
		return userRep.save(user);
	}

	@Override
	public User addRoleToUser(String username, String rolename) {
		User usr = userRep.findByUsername(username);
		if (usr == null)
			return null;

		Role r = roleRep.findFirstByRole(rolename);
		if (r == null)
			return usr;

		if (usr.getRoles() == null)
			usr.setRoles(new ArrayList<>());

		// éviter d'ajouter deux fois le même rôle
		boolean dejaPresent = usr.getRoles().stream()
				.anyMatch(role -> role.getRole().equals(rolename));
		if (!dejaPresent)
			usr.getRoles().add(r);

		return usr;
	}

	@Override
	public Role addRole(Role role) {
		return roleRep.save(role);
	}

	@Override
	public Role findRoleByRole(String rolename) {
		return roleRep.findFirstByRole(rolename);
	}

	@Override
	public User findUserByUsername(String username) {
		return userRep.findByUsername(username);
	}

	@Override
	public List<User> findAllUsers() {
		return userRep.findAll();
	}
}

