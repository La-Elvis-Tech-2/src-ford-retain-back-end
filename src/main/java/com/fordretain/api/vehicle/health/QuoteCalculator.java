package com.fordretain.api.vehicle.health;

import java.util.Comparator;
import java.util.List;

import com.fordretain.api.vehicle.Vehicle;
import com.fordretain.api.vehicle.VehicleComponent;

/**
 * Monta a revisão recomendada: um item para cada componente fora do status
 * "em dia", do mais grave para o mais tranquilo, com peças e mão de obra
 * somadas numa visita só.
 */
public final class QuoteCalculator {

	static final String WARRANTY = "Peças e serviço com 1 ano de garantia na rede Ford.";

	private QuoteCalculator() {
	}

	public static ServiceQuote quote(Vehicle vehicle) {
		List<ServiceQuote.Item> items = vehicle.getComponents().stream()
				.filter(component -> HealthCalculator.statusOf(component.getHealth()) != HealthStatus.OK)
				.sorted(Comparator.comparingInt(VehicleComponent::getHealth))
				.map(QuoteCalculator::item)
				.toList();

		long parts = items.stream().mapToLong(ServiceQuote.Item::partsCents).sum();
		long labor = items.stream().mapToLong(ServiceQuote.Item::laborCents).sum();
		int minutes = vehicle.getComponents().stream()
				.filter(component -> HealthCalculator.statusOf(component.getHealth()) != HealthStatus.OK)
				.mapToInt(component -> component.getType().serviceMinutes())
				.sum();

		return new ServiceQuote(vehicle.getId(), !items.isEmpty(), items, parts, labor, parts + labor, minutes, WARRANTY);
	}

	private static ServiceQuote.Item item(VehicleComponent component) {
		return new ServiceQuote.Item(component.getType(), HealthCalculator.statusOf(component.getHealth()),
				component.getType().service(), component.getType().partsCents(), component.getType().laborCents());
	}
}
