package com.fordretain.api.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token de acesso emitido no login")
public record TokenResponse(
		@Schema(description = "JWT assinado com HS256; envie em Authorization: Bearer <token>") String accessToken,
		@Schema(example = "Bearer") String tokenType,
		@Schema(description = "Validade em segundos", example = "3600") long expiresIn,
		Instant expiresAt,
		UserResponse user) {
}
