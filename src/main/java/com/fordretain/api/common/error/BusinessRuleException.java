package com.fordretain.api.common.error;

import org.springframework.http.HttpStatus;

/** Requisição bem formada, mas que viola uma regra de negócio. */
public class BusinessRuleException extends ApiException {

	public BusinessRuleException(String code, String message) {
		super(HttpStatus.UNPROCESSABLE_CONTENT, code, message);
	}
}
