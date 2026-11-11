package com.mezen.users.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mezen.users.entities.Role;
import com.mezen.users.entities.User;
import com.mezen.users.entities.VerificationToken;
import com.mezen.users.exception.EmailAlreadyExistsException;
import com.mezen.users.exception.ExpiredTokenException;
import com.mezen.users.exception.InvalidTokenException;
import com.mezen.users.exception.UsernameAlreadyExistsException;
import com.mezen.users.register.RegistrationRequest;
import com.mezen.users.repos.RoleRepository;
import com.mezen.users.repos.UserRepository;
import com.mezen.users.repos.VerificationTokenRepository;
import com.mezen.users.util.EmailSender;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private static final String DEFAULT_ROLE = "USER";
	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserRepository userRep;
	private final RoleRepository roleRep;
	private final VerificationTokenRepository verificationTokenRep;
	private final BCryptPasswordEncoder passwordEncoder;
	private final EmailSender emailSender;

	@Override
	public User saveUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		if (user.getEnabled() == null)
			user.setEnabled(true);
		return userRep.save(user);
	}

	@Override
	public User registerUser(RegistrationRequest request) {
		validateRegistration(request);

		String username = request.getUsername().trim();
		String email = request.getEmail().trim().toLowerCase();

		if (userRep.findByEmail(email).isPresent())
			throw new EmailAlreadyExistsException("Email déjà existant !");
		if (userRep.findByUsername(username) != null)
			throw new UsernameAlreadyExistsException("Nom d'utilisateur déjà existant !");

		Role userRole = roleRep.findFirstByRole(DEFAULT_ROLE);
		if (userRole == null)
			throw new IllegalStateException("Rôle USER introuvable, redémarrez l'application.");

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setEnabled(false); // activé après validation de l'email
		List<Role> roles = new ArrayList<>();
		roles.add(userRole);
		user.setRoles(roles);
		userRep.save(user);

		String code = generateCode();
		verificationTokenRep.save(new VerificationToken(code, user));
		sendEmailUser(user, code);

		return user;
	}

	@Override
	public User validateToken(String code) {
		VerificationToken token = verificationTokenRep.findByToken(code);
		if (token == null)
			throw new InvalidTokenException("Code invalide !");

		if (token.getExpirationTime().before(new Date())) {
			verificationTokenRep.delete(token);
			throw new ExpiredTokenException("Code expiré !");
		}

		User user = userRep.findById(token.getUser().getUser_id())
				.orElseThrow(() -> new InvalidTokenException("Utilisateur introuvable !"));
		user.setEnabled(true);
		User savedUser = userRep.save(user);

		// Un code ne peut être utilisé qu'une seule fois.
		verificationTokenRep.delete(token);
		return savedUser;
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

	private void validateRegistration(RegistrationRequest request) {
		if (request == null
				|| isBlank(request.getUsername())
				|| isBlank(request.getEmail())
				|| isBlank(request.getPassword()))
			throw new IllegalArgumentException("Nom d'utilisateur, email et mot de passe sont obligatoires.");

		if (request.getPassword().length() < 6)
			throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères.");
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private String generateCode() {
		int code = 100000 + RANDOM.nextInt(900000);
		return String.valueOf(code);
	}

	private void sendEmailUser(User user, String code) {
		String body = "Bonjour <h1>" + user.getUsername() + "</h1>"
				+ " Votre code de validation est <h1>" + code + "</h1>";
		emailSender.sendEmail(user.getEmail(), body);
	}
}
