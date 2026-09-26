package com.fordretain.api.dto.request;

import com.fordretain.api.model.enums.NewsCategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Novidade publicada para os clientes da rede")
public record NewsRequest(
		@Schema(example = "OFFER") @NotNull(message = "é obrigatória") NewsCategory category,
		@Schema(example = "Alinhamento com 20% de desconto") @NotBlank(message = "é obrigatório") @Size(max = 160, message = "deve ter no máximo 160 caracteres") String title,
		@Schema(example = "Válido em toda a rede até o fim do mês.") @NotBlank(message = "é obrigatório") @Size(max = 300, message = "deve ter no máximo 300 caracteres") String summary,
		@Schema(example = "Agende pelo app e apresente o cupom na recepção.") @NotBlank(message = "é obrigatório") @Size(max = 4000, message = "deve ter no máximo 4000 caracteres") String body) {
}
