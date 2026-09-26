package com.fordretain.api.dto.response;

import java.time.Instant;
import java.util.List;

import com.fordretain.api.exception.ApiExceptionHandler.FieldViolation;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Formato de toda resposta de erro, só para a documentação OpenAPI. Em tempo de
 * execução o corpo é um {@code ProblemDetail} do Spring com os mesmos campos.
 */
@Schema(name = "Problem", description = "Erro no formato Problem Details (RFC 9457)")
public record ProblemResponse(
		@Schema(example = "Não encontrado") String title,
		@Schema(example = "404") int status,
		@Schema(example = "Veículo 99 não encontrado.") String detail,
		@Schema(example = "/vehicles/99") String instance,
		@Schema(description = "Código estável para o cliente tratar o erro", example = "RESOURCE_NOT_FOUND") String code,
		@Schema(example = "2026-09-26T21:00:00Z") Instant timestamp,
		@Schema(description = "Presente só em erros de validação") List<FieldViolation> errors) {
}
