package com.fordretain.api.component;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fordretain.api.dto.response.ServiceQuote;
import com.fordretain.api.model.Vehicle;
import com.fordretain.api.model.VehicleComponent;
import com.fordretain.api.model.enums.HealthStatus;

@Component
public class QuoteCalculator {

	static final String WARRANTY = "Peças e serviço com 1 ano de garantia na rede Ford.";

	public ServiceQuote quote(Vehicle vehicle) {
		List<ServiceQuote.Item> items = vehicle.getComponents().stream()
				.filter(component -> HealthStatus.of(component.getHealth()) != HealthStatus.OK)
				.sorted(Comparator.comparingInt(VehicleComponent::getHealth))
				.map(this::item)
				.toList();

		long parts = items.stream().mapToLong(ServiceQuote.Item::partsCents).sum();
		long labor = items.stream().mapToLong(ServiceQuote.Item::laborCents).sum();
		int minutes = vehicle.getComponents().stream()
				.filter(component -> HealthStatus.of(component.getHealth()) != HealthStatus.OK)
				.mapToInt(component -> component.getType().serviceMinutes())
				.sum();

		return new ServiceQuote(vehicle.getId(), !items.isEmpty(), items, parts, labor, parts + labor, minutes, WARRANTY);
	}

	private ServiceQuote.Item item(VehicleComponent component) {
		return new ServiceQuote.Item(component.getType(), HealthStatus.of(component.getHealth()),
				component.getType().service(), component.getType().partsCents(), component.getType().laborCents());
	}
}
