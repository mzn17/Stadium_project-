package com.mezen.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.mezen.users.entities.Role;
import com.mezen.users.entities.User;
import com.mezen.users.service.UserService;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class UsersMicroserviceApplication {

	@Autowired
	UserService userService;

	public static void main(String[] args) {
		SpringApplication.run(UsersMicroserviceApplication.class, args);
	}

	/**
	 * Initialisation idempotente : ne recrée pas les rôles / utilisateurs
	 * s'ils existent déjà. Sans cela, le démarrage suivant plantait avec
	 * "Duplicate entry 'admin' for key 'UK_...'" et l'application ne démarrait
	 * plus (donc le login échouait).
	 */
	@PostConstruct
	void init_users() {
		// ajouter les rôles s'ils n'existent pas
		if (userService.findRoleByRole("ADMIN") == null)
			userService.addRole(new Role(null, "ADMIN"));
		if (userService.findRoleByRole("USER") == null)
			userService.addRole(new Role(null, "USER"));

		// ajouter les utilisateurs s'ils n'existent pas
		createUserIfAbsent("admin", "123");
		createUserIfAbsent("mezen", "123");
		createUserIfAbsent("yassine", "123");

		// ajouter les rôles aux utilisateurs (sans doublon)
		userService.addRoleToUser("admin", "ADMIN");
		userService.addRoleToUser("admin", "USER");
		userService.addRoleToUser("mezen", "USER");
		userService.addRoleToUser("yassine", "USER");
	}

	private void createUserIfAbsent(String username, String password) {
		if (userService.findUserByUsername(username) == null) {
			userService.saveUser(new User(null, username, password, true, null));
		}
	}
}

