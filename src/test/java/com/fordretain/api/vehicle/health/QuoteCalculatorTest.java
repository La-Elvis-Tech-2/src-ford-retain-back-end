package com.fordretain.api.vehicle.health;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fordretain.api.vehicle.ComponentType;
import com.fordretain.api.vehicle.Vehicle;

@DisplayName("Revisão recomendada")
class QuoteCalculatorTest {

	@Test
	@DisplayName("Orça a revisão da Ranger em R$ 1.595 com 1h30 de serviço, como no app")
	void rangerQuote() {
		ServiceQuote quote = QuoteCalculator.quote(HealthCalculatorTest.ranger());

		assertThat(quote.serviceRecommended()).isTrue();
		assertThat(quote.items()).extracting(ServiceQuote.Item::component).containsExactly(ComponentType.OIL,
				ComponentType.BRAKE_PAD, ComponentType.BATTERY, ComponentType.AIR_FILTER, ComponentType.CABIN_FILTER);
		assertThat(quote.partsCents()).isEqualTo(1_305_00);
		assertThat(quote.laborCents()).isEqualTo(290_00);
		assertThat(quote.totalCents()).isEqualTo(1_595_00);
		assertThat(quote.durationMinutes()).isEqualTo(90);
	}

	@Test
	@DisplayName("Não recomenda revisão quando todos os componentes estão em dia")
	void noServiceWhenEverythingIsOk() {
		Vehicle vehicle = new Vehicle(null, "ABC1D23", "Ka", 2020, "Prata", 50_000, Instant.EPOCH);
		vehicle.initializeComponents(Instant.EPOCH);

		ServiceQuote quote = QuoteCalculator.quote(vehicle);

		assertThat(quote.serviceRecommended()).isFalse();
		assertThat(quote.items()).isEmpty();
		assertThat(quote.totalCents()).isZero();
	}
}
