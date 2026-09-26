package com.fordretain.api.exception;

import org.springframework.http.HttpStatus;

/** A operação conflita com o estado atual do recurso (duplicidade, transição inválida). */
public class ConflictException extends ApiException {

	public ConflictException(String code, String message) {
		super(HttpStatus.CONFLICT, code, message);
	}
}
