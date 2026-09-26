package com.fordretain.api.vehicle.health;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.fordretain.api.vehicle.Vehicle;
import com.fordretain.api.vehicle.VehicleComponent;
import com.fordretain.api.vehicle.VehicleSystem;
import com.fordretain.api.vehicle.health.HealthReport.ComponentHealth;
import com.fordretain.api.vehicle.health.HealthReport.Score;
import com.fordretain.api.vehicle.health.HealthReport.StatusCount;
import com.fordretain.api.vehicle.health.HealthReport.SystemHealth;

/**
 * As contas do laudo, iguais às do app mobile.
 *
 * <ul>
 * <li>Nota abaixo de 40: urgente; abaixo de 75: atenção; o resto está em dia.</li>
 * <li>Nota de um conjunto: média simples das notas, arredondada.</li>
 * <li>Variação: diferença entre as médias JÁ arredondadas de hoje e da semana
 * anterior, para o número exibido sempre bater com a conta que o cliente faz.</li>
 * <li>Status de um conjunto: o pior status entre os componentes.</li>
 * </ul>
 */
public final class HealthCalculator {

	static final int URGENT_BELOW = 40;
	static final int ATTENTION_BELOW = 75;

	private HealthCalculator() {
	}

	public static HealthStatus statusOf(int health) {
		if (health < URGENT_BELOW) {
			return HealthStatus.URGENT;
		}
		return health < ATTENTION_BELOW ? HealthStatus.ATTENTION : HealthStatus.OK;
	}

	public static Score scoreOf(List<VehicleComponent> components) {
		int score = roundedMean(components.stream().mapToInt(VehicleComponent::getHealth).toArray());
		int lastWeek = roundedMean(components.stream().mapToInt(VehicleComponent::getHealthLastWeek).toArray());
		return new Score(score, score - lastWeek, worstStatus(components));
	}

	public static HealthStatus worstStatus(List<VehicleComponent> components) {
		HealthStatus worst = HealthStatus.OK;
		for (VehicleComponent component : components) {
			HealthStatus status = statusOf(component.getHealth());
			if (status.isWorseThan(worst)) {
				worst = status;
			}
		}
		return worst;
	}

	public static HealthReport report(Vehicle vehicle) {
		List<VehicleComponent> components = vehicle.getComponents();

		List<SystemHealth> systems = Arrays.stream(VehicleSystem.values())
				.map(system -> system(system, components))
				.toList();

		Instant readAt = components.stream().map(VehicleComponent::getUpdatedAt).max(Comparator.naturalOrder())
				.orElse(vehicle.getCreatedAt());

		return new HealthReport(vehicle.getId(), readAt, scoreOf(components), count(components), systems);
	}

	private static SystemHealth system(VehicleSystem system, List<VehicleComponent> all) {
		List<VehicleComponent> members = all.stream().filter(component -> component.getType().system() == system).toList();
		Score score = scoreOf(members);
		List<ComponentHealth> components = members.stream()
				.sorted(Comparator.comparingInt(VehicleComponent::getHealth))
				.map(HealthCalculator::component)
				.toList();
		return new SystemHealth(system, system.label(), score.score(), score.delta(), score.status(), components);
	}

	private static ComponentHealth component(VehicleComponent component) {
		return new ComponentHealth(component.getType(), component.getType().label(), component.getHealth(),
				component.getHealthLastWeek(), statusOf(component.getHealth()), component.getDetail());
	}

	private static StatusCount count(List<VehicleComponent> components) {
		int urgent = 0;
		int attention = 0;
		int ok = 0;
		for (VehicleComponent component : components) {
			switch (statusOf(component.getHealth())) {
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
