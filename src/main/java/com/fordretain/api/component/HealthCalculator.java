package com.fordretain.api.component;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fordretain.api.dto.response.HealthReport;
import com.fordretain.api.dto.response.HealthReport.ComponentHealth;
import com.fordretain.api.dto.response.HealthReport.Score;
import com.fordretain.api.dto.response.HealthReport.StatusCount;
import com.fordretain.api.dto.response.HealthReport.SystemHealth;
import com.fordretain.api.model.Vehicle;
import com.fordretain.api.model.VehicleComponent;
import com.fordretain.api.model.enums.HealthStatus;
import com.fordretain.api.model.enums.VehicleSystem;

@Component
public class HealthCalculator {

	public Score scoreOf(List<VehicleComponent> components) {
		int score = roundedMean(components.stream().mapToInt(VehicleComponent::getHealth).toArray());
		int lastWeek = roundedMean(components.stream().mapToInt(VehicleComponent::getHealthLastWeek).toArray());
		return new Score(score, score - lastWeek, worstStatus(components));
	}

	public HealthStatus worstStatus(List<VehicleComponent> components) {
		HealthStatus worst = HealthStatus.OK;
		for (VehicleComponent component : components) {
			HealthStatus status = HealthStatus.of(component.getHealth());
			if (status.isWorseThan(worst)) {
				worst = status;
			}
		}
		return worst;
	}

	public HealthReport report(Vehicle vehicle) {
		List<VehicleComponent> components = vehicle.getComponents();

		List<SystemHealth> systems = Arrays.stream(VehicleSystem.values())
				.map(system -> system(system, components))
				.toList();

		Instant readAt = components.stream().map(VehicleComponent::getUpdatedAt).max(Comparator.naturalOrder())
				.orElse(vehicle.getCreatedAt());

		return new HealthReport(vehicle.getId(), readAt, scoreOf(components), count(components), systems);
	}

	private SystemHealth system(VehicleSystem system, List<VehicleComponent> all) {
		List<VehicleComponent> members = all.stream().filter(component -> component.getType().system() == system).toList();
		Score score = scoreOf(members);
		List<ComponentHealth> components = members.stream()
				.sorted(Comparator.comparingInt(VehicleComponent::getHealth))
				.map(this::component)
				.toList();
		return new SystemHealth(system, system.label(), score.score(), score.delta(), score.status(), components);
	}

	private ComponentHealth component(VehicleComponent component) {
		return new ComponentHealth(component.getType(), component.getType().label(), component.getHealth(),
				component.getHealthLastWeek(), HealthStatus.of(component.getHealth()), component.getDetail());
	}

	private StatusCount count(List<VehicleComponent> components) {
		int urgent = 0;
		int attention = 0;
		int ok = 0;
		for (VehicleComponent component : components) {
			switch (HealthStatus.of(component.getHealth())) {
				case URGENT -> urgent++;
				case ATTENTION -> attention++;
				case OK -> ok++;
			}
		}
		return new StatusCount(urgent, attention, ok);
	}

	private static int roundedMean(int[] values) {
		if (values.length == 0) {
			return 0;
		}
		return (int) Math.round(Arrays.stream(values).average().orElse(0));
	}
}
