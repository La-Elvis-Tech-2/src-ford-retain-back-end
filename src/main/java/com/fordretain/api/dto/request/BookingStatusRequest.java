package com.fordretain.api.dto.request;

import com.fordretain.api.model.enums.BookingStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Mudança de status do agendamento")
public record BookingStatusRequest(
		@Schema(example = "CONFIRMED") @NotNull(message = "é obrigatório") BookingStatus status) {
}
