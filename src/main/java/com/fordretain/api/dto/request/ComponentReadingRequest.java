package com.fordretain.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Nova leitura de um componente, enviada pela integração com os módulos do veículo")
public record ComponentReadingRequest(
		@Schema(description = "Nota de 0 a 100", example = "72") @NotNull(message = "é obrigatória") @Min(value = 0, message = "deve ser no mínimo 0") @Max(value = 100, message = "deve ser no máximo 100") Integer health,
		@Schema(example = "Sulco médio de 3,9 mm") @Size(max = 200, message = "deve ter no máximo 200 caracteres") String detail) {
}
