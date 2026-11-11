package com.mezen.users.restControllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mezen.users.entities.User;
import com.mezen.users.register.RegistrationRequest;
import com.mezen.users.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RegistrationController {

	private final UserService userService;

	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody RegistrationRequest request) {
		return ResponseEntity.ok(userService.registerUser(request));
	}

	@GetMapping("/verifyEmail/{code}")
	public User verifyEmail(@PathVariable String code) {
		return userService.validateToken(code);
	}
}
