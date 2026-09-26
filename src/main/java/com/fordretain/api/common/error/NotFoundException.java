package com.fordretain.api.common.error;

import org.springframework.http.HttpStatus;

/** Recurso inexistente, ou que o usuário autenticado não tem permissão de ver. */
public class NotFoundException extends ApiException {

	public NotFoundException(String resource, Object id) {
		super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", resource + " " + id + " não encontrado.");
	}
}
