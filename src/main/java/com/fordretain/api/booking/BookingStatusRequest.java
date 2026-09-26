package com.fordretain.api.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Mudança de status do agendamento")
public record BookingStatusRequest(
		@Schema(example = "CONFIRMED") @NotNull(message = "é obrigatório") BookingStatus status) {
}
