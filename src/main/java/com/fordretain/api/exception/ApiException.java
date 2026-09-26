package com.fordretain.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Erro de negócio com status HTTP e código estável. O {@link ApiExceptionHandler}
 * converte qualquer subclasse no mesmo formato de resposta.
 */
public abstract class ApiException extends RuntimeException {

	private final HttpStatus status;
	private final String code;

	protected ApiException(HttpStatus status, String code, String message) {
		super(message);
		this.status = status;
		this.code = code;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}
}
