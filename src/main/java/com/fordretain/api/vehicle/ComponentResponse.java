package com.fordretain.api.vehicle;

import java.time.Instant;

import com.fordretain.api.vehicle.health.HealthCalculator;
import com.fordretain.api.vehicle.health.HealthStatus;

public record ComponentResponse(ComponentType type, String name, VehicleSystem system, int health,
		int healthLastWeek, HealthStatus status, String detail, Instant updatedAt) {

	public static ComponentResponse from(VehicleComponent component) {
		return new ComponentResponse(component.getType(), component.getType().label(), component.getType().system(),
				component.getHealth(), component.getHealthLastWeek(), HealthCalculator.statusOf(component.getHealth()),
				component.getDetail(), component.getUpdatedAt());
	}
}
