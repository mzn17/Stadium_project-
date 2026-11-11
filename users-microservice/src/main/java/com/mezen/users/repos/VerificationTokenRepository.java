package com.mezen.users.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mezen.users.entities.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
	VerificationToken findByToken(String token);
}
