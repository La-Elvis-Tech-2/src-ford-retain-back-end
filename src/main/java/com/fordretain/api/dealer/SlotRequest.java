package com.fordretain.api.dealer;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Novo horário disponível na oficina")
public record SlotRequest(
		@Schema(description = "Início do atendimento em ISO-8601", example = "2026-10-05T12:00:00Z") @NotNull(message = "é obrigatório") Instant startsAt) {
}
