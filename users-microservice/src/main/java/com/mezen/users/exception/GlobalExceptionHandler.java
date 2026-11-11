package com.mezen.users.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ErrorDetails> handleEmailAlreadyExistsException(
			EmailAlreadyExistsException exception, WebRequest webRequest) {
		return build(exception, webRequest, "USER_EMAIL_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UsernameAlreadyExistsException.class)
	public ResponseEntity<ErrorDetails> handleUsernameAlreadyExistsException(
			UsernameAlreadyExistsException exception, WebRequest webRequest) {
		return build(exception, webRequest, "USERNAME_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ErrorDetails> handleInvalidTokenException(
			InvalidTokenException exception, WebRequest webRequest) {
		return build(exception, webRequest, "INVALID_TOKEN", HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ExpiredTokenException.class)
	public ResponseEntity<ErrorDetails> handleExpiredTokenException(
			ExpiredTokenException exception, WebRequest webRequest) {
		return build(exception, webRequest, "EXPIRED_TOKEN", HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorDetails> handleIllegalArgumentException(
			IllegalArgumentException exception, WebRequest webRequest) {
		return build(exception, webRequest, "INVALID_REQUEST", HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception, WebRequest webRequest) {
		return build(exception, webRequest, "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<ErrorDetails> build(Exception exception, WebRequest webRequest,
			String errorCode, HttpStatus status) {
		ErrorDetails details = new ErrorDetails(
				LocalDateTime.now(),
				exception.getMessage(),
				webRequest.getDescription(false),
				errorCode);
		return new ResponseEntity<>(details, status);
	}
}
