package com.testoptimal.server.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.ServletException;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
@SuppressWarnings("serial")
public class AuthenticationException extends ServletException {

	public AuthenticationException (String msg) {
		super (msg);
	}
}
