package com.fordretain.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados editáveis do veículo. A placa não muda.")
public record VehicleUpdateRequest(
		@NotBlank(message = "é obrigatório") @Size(max = 80, message = "deve ter no máximo 80 caracteres") String model,
		@NotNull(message = "é obrigatório") @Min(value = 1950, message = "deve ser 1950 ou posterior") @Max(value = 2100, message = "deve ser 2100 ou anterior") Integer modelYear,
		@NotBlank(message = "é obrigatória") @Size(max = 40, message = "deve ter no máximo 40 caracteres") String color,
		@NotNull(message = "é obrigatória") @PositiveOrZero(message = "não pode ser negativa") Integer mileageKm) {
}
