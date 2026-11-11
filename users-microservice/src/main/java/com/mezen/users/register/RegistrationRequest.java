package com.mezen.users.register;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Données envoyées par le formulaire d'inscription Angular. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
	private String username;
	private String password;
	private String email;
}
