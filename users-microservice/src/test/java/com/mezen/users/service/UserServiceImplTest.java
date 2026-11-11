package com.mezen.users.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository userRep;
	@Mock
	private RoleRepository roleRep;
	@Mock
	private VerificationTokenRepository verificationTokenRep;
	@Mock
	private BCryptPasswordEncoder passwordEncoder;
	@Mock
	private EmailSender emailSender;

	@InjectMocks
	private UserServiceImpl userService;

	private final Role userRole = new Role(2L, "USER");

	@Test
	void registerUser_createsDisabledUserWithUserRoleAndSendsCode() {
		RegistrationRequest request = new RegistrationRequest("newuser", "secret123", "New@Example.com");

		when(userRep.findByEmail("new@example.com")).thenReturn(Optional.empty());
		when(userRep.findByUsername("newuser")).thenReturn(null);
		when(roleRep.findFirstByRole("USER")).thenReturn(userRole);
		when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");

		User result = userService.registerUser(request);

		assertEquals("newuser", result.getUsername());
		assertEquals("new@example.com", result.getEmail());
		assertEquals("hashed-password", result.getPassword());
		assertFalse(result.getEnabled());
		assertEquals(List.of(userRole), result.getRoles());

		ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
		verify(verificationTokenRep).save(tokenCaptor.capture());
		VerificationToken token = tokenCaptor.getValue();
		assertTrue(token.getToken().matches("\\d{6}"));
		assertTrue(token.getExpirationTime().after(new Date()));

		verify(emailSender).sendEmail(eq("new@example.com"), contains(token.getToken()));
	}

	@Test
	void registerUser_rejectsDuplicateEmail() {
		RegistrationRequest request = new RegistrationRequest("newuser", "secret123", "used@example.com");
		when(userRep.findByEmail("used@example.com")).thenReturn(Optional.of(new User()));

		assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(request));

		verify(userRep, never()).save(any(User.class));
		verify(emailSender, never()).sendEmail(anyString(), anyString());
	}

	@Test
	void registerUser_rejectsDuplicateUsername() {
		RegistrationRequest request = new RegistrationRequest("admin", "secret123", "new@example.com");
		when(userRep.findByEmail("new@example.com")).thenReturn(Optional.empty());
		when(userRep.findByUsername("admin")).thenReturn(new User());

		assertThrows(UsernameAlreadyExistsException.class, () -> userService.registerUser(request));

		verify(userRep, never()).save(any(User.class));
	}

	@Test
	void registerUser_rejectsShortPassword() {
		RegistrationRequest request = new RegistrationRequest("newuser", "123", "new@example.com");

		assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));

		verify(userRep, never()).save(any(User.class));
	}

	@Test
	void validateToken_enablesUserAndConsumesToken() {
		User user = new User(1L, "newuser", "hashed", false, "new@example.com", null);
		VerificationToken token = new VerificationToken("123456", user);
		when(verificationTokenRep.findByToken("123456")).thenReturn(token);
		when(userRep.findById(1L)).thenReturn(Optional.of(user));
		when(userRep.save(user)).thenReturn(user);

		User result = userService.validateToken("123456");

		assertTrue(result.getEnabled());
		verify(userRep).save(user);
		verify(verificationTokenRep).delete(token);
	}

	@Test
	void validateToken_rejectsUnknownCode() {
		when(verificationTokenRep.findByToken("000000")).thenReturn(null);

		assertThrows(InvalidTokenException.class, () -> userService.validateToken("000000"));
	}

	@Test
	void validateToken_rejectsExpiredCode() {
		User user = new User(1L, "newuser", "hashed", false, "new@example.com", null);
		VerificationToken token = new VerificationToken("123456", user);
		token.setExpirationTime(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)));
		when(verificationTokenRep.findByToken("123456")).thenReturn(token);

		assertThrows(ExpiredTokenException.class, () -> userService.validateToken("123456"));

		verify(verificationTokenRep).delete(token);
		verify(userRep, never()).save(any(User.class));
	}
}
