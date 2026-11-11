package com.mezen.users.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mezen.users.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
	// La base peut contenir plusieurs lignes "ADMIN" (anciennes initialisations) :
	// findFirst évite une NonUniqueResultException.
	Role findFirstByRole(String role);
}

