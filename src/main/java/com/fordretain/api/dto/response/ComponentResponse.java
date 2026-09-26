package com.fordretain.api.dto.response;

import java.time.Instant;

import com.fordretain.api.model.VehicleComponent;
import com.fordretain.api.model.enums.ComponentType;
import com.fordretain.api.model.enums.HealthStatus;
import com.fordretain.api.model.enums.VehicleSystem;

public record ComponentResponse(ComponentType type, String name, VehicleSystem system, int health,
		int healthLastWeek, HealthStatus status, String detail, Instant updatedAt) {

	public static ComponentResponse from(VehicleComponent component) {
		return new ComponentResponse(component.getType(), component.getType().label(), component.getType().system(),
				component.getHealth(), component.getHealthLastWeek(), HealthStatus.of(component.getHealth()),
				component.getDetail(), component.getUpdatedAt());
	}
}
