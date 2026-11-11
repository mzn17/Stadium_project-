package com.mezen.users.service;

import java.util.List;

import com.mezen.users.entities.Role;
import com.mezen.users.entities.User;
import com.mezen.users.register.RegistrationRequest;

public interface UserService {
	User saveUser(User user);

	User registerUser(RegistrationRequest request);

	User validateToken(String code);

	User findUserByUsername(String username);

	Role addRole(Role role);

	Role findRoleByRole(String rolename);

	User addRoleToUser(String username, String rolename);

	List<User> findAllUsers();
}
