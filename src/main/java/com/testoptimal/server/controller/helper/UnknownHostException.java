package com.testoptimal.server.controller.helper;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.ServletException;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnknownHostException extends ServletException {

	public UnknownHostException (String msg) {
		super (msg);
	}
}



