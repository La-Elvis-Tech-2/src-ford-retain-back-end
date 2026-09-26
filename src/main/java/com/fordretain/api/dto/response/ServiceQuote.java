package com.fordretain.api.dto.response;

import java.util.List;

import com.fordretain.api.model.enums.ComponentType;
import com.fordretain.api.model.enums.HealthStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Revisão recomendada com preço fechado, em centavos de real")
public record ServiceQuote(
		Long vehicleId,
		@Schema(description = "Falso quando nenhum componente pede serviço") boolean serviceRecommended,
		List<Item> items,
		long partsCents,
		long laborCents,
		long totalCents,
		int durationMinutes,
		String warranty) {

	public record Item(ComponentType component, HealthStatus status, String description, long partsCents,
			long laborCents) {
	}
}
