package com.fordretain.api.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de uma concessionária")
public record DealerRequest(
		@Schema(example = "Ford Mooca") @NotBlank(message = "é obrigatório") @Size(max = 120, message = "deve ter no máximo 120 caracteres") String name,
		@Schema(example = "R. da Mooca, 2.500") @NotBlank(message = "é obrigatório") @Size(max = 200, message = "deve ter no máximo 200 caracteres") String address,
		@Schema(example = "Mooca, São Paulo") @NotBlank(message = "é obrigatório") @Size(max = 120, message = "deve ter no máximo 120 caracteres") String district,
		@Schema(example = "4.6") @NotNull(message = "é obrigatória") @DecimalMin(value = "0.0", message = "deve ser no mínimo 0") @DecimalMax(value = "5.0", message = "deve ser no máximo 5") @Digits(integer = 1, fraction = 1, message = "deve ter uma casa decimal") BigDecimal rating,
		@Schema(example = "540") @NotNull(message = "é obrigatório") @PositiveOrZero(message = "não pode ser negativo") Integer reviewCount) {
}
