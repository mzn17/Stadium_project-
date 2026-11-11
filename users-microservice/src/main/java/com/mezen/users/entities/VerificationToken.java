package com.mezen.users.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {

	/** Durée de validité du code, en minutes. */
	private static final int EXPIRATION_TIME = 15;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 6)
	private String token;

	@Column(nullable = false)
	private Date expirationTime;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	public VerificationToken(String token, User user) {
		this.token = token;
		this.user = user;
		this.expirationTime = Date.from(Instant.now().plus(EXPIRATION_TIME, ChronoUnit.MINUTES));
	}
}
