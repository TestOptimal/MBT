package com.testoptimal.server.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * when server failed to start.
 * 
 * @author yxl01
 *
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
@SuppressWarnings("serial")
public class StartupException extends Exception {
	public StartupException (String msg) {
		super (msg);
	}
}
