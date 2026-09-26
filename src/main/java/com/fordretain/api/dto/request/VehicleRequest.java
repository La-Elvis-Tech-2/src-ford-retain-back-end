package com.fordretain.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Cadastro de veículo do cliente")
public record VehicleRequest(
		@Schema(description = "Placa antiga (ABC-1234) ou Mercosul (ABC1D23)", example = "FRD2B26") @NotBlank(message = "é obrigatória") @Pattern(regexp = VehicleRequest.PLATE, message = "deve seguir o formato ABC-1234 ou ABC1D23") String plate,
		@Schema(example = "Ranger XLS 2.2") @NotBlank(message = "é obrigatório") @Size(max = 80, message = "deve ter no máximo 80 caracteres") String model,
		@Schema(example = "2024") @NotNull(message = "é obrigatório") @Min(value = 1950, message = "deve ser 1950 ou posterior") @Max(value = 2100, message = "deve ser 2100 ou anterior") Integer modelYear,
		@Schema(example = "Azul Belize") @NotBlank(message = "é obrigatória") @Size(max = 40, message = "deve ter no máximo 40 caracteres") String color,
		@Schema(example = "12500") @NotNull(message = "é obrigatória") @PositiveOrZero(message = "não pode ser negativa") Integer mileageKm) {

	/** Três letras, um dígito, letra ou dígito, dois dígitos; hífen opcional. */
	public static final String PLATE = "^[A-Za-z]{3}-?[0-9][A-Za-z0-9][0-9]{2}$";
}
