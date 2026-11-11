package com.mezen.users.service;

import java.util.List;
import com.mezen.users.entities.Role;
import com.mezen.users.entities.User;

public interface UserService {
	User saveUser(User user);
	User findUserByUsername(String username);
	Role addRole(Role role);
	Role findRoleByRole(String rolename);
	User addRoleToUser(String username, String rolename);
	List<User> findAllUsers();
}

