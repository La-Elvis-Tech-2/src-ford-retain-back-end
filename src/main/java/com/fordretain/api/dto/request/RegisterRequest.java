package com.fordretain.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Cadastro de cliente")
public record RegisterRequest(
		@Schema(example = "Maria Oliveira") @NotBlank(message = "é obrigatório") @Size(max = 120, message = "deve ter no máximo 120 caracteres") String name,
		@Schema(example = "maria@exemplo.com") @NotBlank(message = "é obrigatório") @Email(message = "deve ser um e-mail válido") @Size(max = 160, message = "deve ter no máximo 160 caracteres") String email,
		@Schema(example = "Senha@2026") @NotBlank(message = "é obrigatória") @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres") String password) {
}
