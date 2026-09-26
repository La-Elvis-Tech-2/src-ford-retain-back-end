package com.fordretain.api.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fordretain.api.dto.response.HealthReport;
import com.fordretain.api.dto.response.HealthReport.SystemHealth;
import com.fordretain.api.model.Vehicle;
import com.fordretain.api.model.enums.ComponentType;
import com.fordretain.api.model.enums.HealthStatus;
import com.fordretain.api.model.enums.VehicleSystem;

@DisplayName("Cálculo do laudo de saúde")
class HealthCalculatorTest {

	private static final Instant NOW = Instant.parse("2026-09-26T10:12:00Z");

	private final HealthCalculator calculator = new HealthCalculator();

	@ParameterizedTest(name = "nota {0} -> {1}")
	@CsvSource({ "0, URGENT", "39, URGENT", "40, ATTENTION", "74, ATTENTION", "75, OK", "100, OK" })
	@DisplayName("Converte a nota em status nos limites de 40 e 75")
	void statusThresholds(int health, HealthStatus expected) {
		assertThat(HealthStatus.of(health)).isEqualTo(expected);
	}

	@Test
	@DisplayName("Reproduz o laudo da Ranger do app: 67 geral, com variação de -2")
	void rangerReport() {
		HealthReport report = calculator.report(ranger());

		assertThat(report.overall().score()).isEqualTo(67);
		assertThat(report.overall().delta()).isEqualTo(-2);
		assertThat(report.overall().status()).isEqualTo(HealthStatus.URGENT);
		assertThat(report.counts()).isEqualTo(new HealthReport.StatusCount(2, 3, 5));
		assertThat(report.systems()).extracting(SystemHealth::system, SystemHealth::score, SystemHealth::delta,
				SystemHealth::status)
				.containsExactly(
						tuple(VehicleSystem.ENGINE, 69, -2, HealthStatus.URGENT),
						tuple(VehicleSystem.BRAKES, 60, -2, HealthStatus.URGENT),
						tuple(VehicleSystem.BATTERY, 55, -3, HealthStatus.ATTENTION),
						tuple(VehicleSystem.TIRES, 84, 2, HealthStatus.OK));
	}

	@Test
	@DisplayName("A variação é a diferença entre as médias já arredondadas")
	void deltaUsesRoundedMeans() {
		Vehicle vehicle = new Vehicle(null, "ABC1D23", "Ka", 2020, "Prata", 50_000, NOW);
		// hoje: (31 + 88) / 2 = 59,5 -> 60 · semana anterior: (35 + 88) / 2 = 61,5 -> 62
		vehicle.addReading(ComponentType.BRAKE_PAD, 31, 35, null, NOW);
		vehicle.addReading(ComponentType.BRAKE_FLUID, 88, 88, null, NOW);

		HealthReport.Score score = calculator.scoreOf(vehicle.getComponents());

		assertThat(score.score()).isEqualTo(60);
		assertThat(score.delta()).isEqualTo(-2);
	}

	@Test
	@DisplayName("O status de um sistema é o pior entre os seus componentes")
	void systemTakesWorstStatus() {
		Vehicle vehicle = new Vehicle(null, "ABC1D23", "Ka", 2020, "Prata", 50_000, NOW);
		vehicle.addReading(ComponentType.OIL, 95, 95, null, NOW);
		vehicle.addReading(ComponentType.AIR_FILTER, 60, 60, null, NOW);

		assertThat(calculator.worstStatus(vehicle.getComponents())).isEqualTo(HealthStatus.ATTENTION);
	}

	@Test
	@DisplayName("Ordena os componentes de cada sistema do mais grave para o mais tranquilo")
	void componentsSortedByUrgency() {
		HealthReport report = calculator.report(ranger());

		List<Integer> engine = report.systems().get(0).components().stream().map(HealthReport.ComponentHealth::health).toList();
		assertThat(engine).containsExactly(18, 58, 64, 86, 92, 95);
	}

	static Vehicle ranger() {
		Vehicle ranger = new Vehicle(null, "BRA2E19", "Ranger Limited 2.0", 2023, "Preto Sublime", 78_420, NOW);
		ranger.addReading(ComponentType.OIL, 18, 26, null, NOW);
		ranger.addReading(ComponentType.BRAKE_PAD, 31, 35, null, NOW);
		ranger.addReading(ComponentType.BATTERY, 55, 58, null, NOW);
		ranger.addReading(ComponentType.AIR_FILTER, 58, 61, null, NOW);
		ranger.addReading(ComponentType.CABIN_FILTER, 64, 66, null, NOW);
		ranger.addReading(ComponentType.TIRES, 84, 82, null, NOW);
		ranger.addReading(ComponentType.SPARK_PLUGS, 86, 87, null, NOW);
		ranger.addReading(ComponentType.BRAKE_FLUID, 88, 88, null, NOW);
		ranger.addReading(ComponentType.TIMING_BELT, 92, 92, null, NOW);
		ranger.addReading(ComponentType.COOLANT, 95, 95, null, NOW);
		return ranger;
	}
}
