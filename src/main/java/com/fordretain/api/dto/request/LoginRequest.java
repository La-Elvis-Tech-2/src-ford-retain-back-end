package com.fordretain.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de acesso")
public record LoginRequest(
		@Schema(example = "cliente@fordretain.com") @NotBlank(message = "é obrigatório") String email,
		@Schema(example = "Cliente@123") @NotBlank(message = "é obrigatória") String password) {
}
