package com.fordretain.api.vehicle.health;

import java.time.Instant;
import java.util.List;

import com.fordretain.api.vehicle.ComponentType;
import com.fordretain.api.vehicle.VehicleSystem;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Laudo de saúde do veículo calculado a partir da leitura dos componentes")
public record HealthReport(
		Long vehicleId,
		@Schema(description = "Momento da leitura mais recente") Instant readAt,
		Score overall,
		StatusCount counts,
		List<SystemHealth> systems) {

	@Schema(description = "Nota de 0 a 100, variação contra a semana anterior e o pior status do conjunto")
	public record Score(int score, int delta, HealthStatus status) {
	}

	public record StatusCount(int urgent, int attention, int ok) {
	}

	public record SystemHealth(VehicleSystem system, String name, int score, int delta, HealthStatus status,
			List<ComponentHealth> components) {
	}

	public record ComponentHealth(ComponentType type, String name, int health, int healthLastWeek,
			HealthStatus status, String detail) {
	}
}
