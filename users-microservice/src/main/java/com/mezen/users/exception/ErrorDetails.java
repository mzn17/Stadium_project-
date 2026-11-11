package com.mezen.users.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Format JSON unique renvoyé au front Angular en cas d'erreur. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {
	private LocalDateTime timestamp;
	private String message;
	private String path;
	private String errorCode;
}
