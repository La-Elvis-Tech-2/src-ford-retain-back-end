package com.fordretain.api.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Reserva de um horário para a revisão de um veículo do cliente")
public record BookingRequest(
		@Schema(example = "1") @NotNull(message = "é obrigatório") Long vehicleId,
		@Schema(example = "1") @NotNull(message = "é obrigatório") Long slotId) {
}
