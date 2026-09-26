package com.fordretain.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Login recusado. A mensagem é a mesma para e-mail inexistente e senha errada,
 * para não revelar quais e-mails têm conta.
 */
public class InvalidCredentialsException extends ApiException {

	public InvalidCredentialsException() {
		super(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "E-mail ou senha inválidos.");
	}
}
