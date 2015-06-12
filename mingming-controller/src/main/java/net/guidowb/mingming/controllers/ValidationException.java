package net.guidowb.mingming.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Request Validation Error")
public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ValidationException(String message) { super(message); }
	public ValidationException(String format, Object... arguments) { super(String.format(format, arguments)); }
}
