package com.fordretain.api.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {

	public InvalidCredentialsException() {
		super(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "E-mail ou senha inválidos.");
	}
}
