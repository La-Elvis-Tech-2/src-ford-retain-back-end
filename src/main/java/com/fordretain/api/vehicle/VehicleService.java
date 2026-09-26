package com.fordretain.api.vehicle;

import java.time.Clock;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fordretain.api.booking.BookingRepository;
import com.fordretain.api.booking.BookingStatus;
import com.fordretain.api.common.error.BusinessRuleException;
import com.fordretain.api.common.error.ConflictException;
import com.fordretain.api.common.error.NotFoundException;
import com.fordretain.api.security.AuthenticatedUser;
import com.fordretain.api.user.Role;
import com.fordretain.api.user.User;
import com.fordretain.api.user.UserRepository;
import com.fordretain.api.vehicle.health.HealthCalculator;
import com.fordretain.api.vehicle.health.HealthReport;
import com.fordretain.api.vehicle.health.QuoteCalculator;
import com.fordretain.api.vehicle.health.ServiceQuote;

/**
 * Veículos e laudos.
 *
 * O cliente só enxerga os próprios veículos: o de outra pessoa responde 404,
 * como se não existisse, para não confirmar que aquela placa está cadastrada.
 * O administrador enxerga todos.
 */
@Service
@Transactional(readOnly = true)
public class VehicleService {

	private final VehicleRepository vehicles;
	private final UserRepository users;
	private final BookingRepository bookings;
	private final HealthCalculator healthCalculator;
	private final QuoteCalculator quoteCalculator;
	private final Clock clock;

	public VehicleService(VehicleRepository vehicles, UserRepository users, BookingRepository bookings,
			HealthCalculator healthCalculator, QuoteCalculator quoteCalculator, Clock clock) {
		this.vehicles = vehicles;
		this.users = users;
		this.bookings = bookings;
		this.healthCalculator = healthCalculator;
		this.quoteCalculator = quoteCalculator;
		this.clock = clock;
	}

	public List<Vehicle> list(AuthenticatedUser user) {
		return user.is(Role.ADMIN) ? vehicles.findAllByOrderByIdAsc() : vehicles.findByOwnerIdOrderByIdAsc(user.id());
	}

	public Vehicle get(Long id, AuthenticatedUser user) {
		return vehicles.findById(id)
				.filter(vehicle -> user.is(Role.ADMIN) || vehicle.isOwnedBy(user.id()))
				.orElseThrow(() -> new NotFoundException("Veículo", id));
	}

	@Transactional
	public Vehicle create(VehicleRequest request, AuthenticatedUser user) {
		String plate = normalizePlate(request.plate());
		if (vehicles.existsByPlate(plate)) {
			throw new ConflictException("PLATE_ALREADY_REGISTERED", "Esta placa já está cadastrada.");
		}
		User owner = users.getReferenceById(user.id());
		Vehicle vehicle = new Vehicle(owner, plate, request.model().trim(), request.modelYear(), request.color().trim(),
				request.mileageKm(), clock.instant());
		vehicle.initializeComponents(clock.instant());
		return vehicles.save(vehicle);
	}

	@Transactional
	public Vehicle update(Long id, VehicleUpdateRequest request, AuthenticatedUser user) {
		Vehicle vehicle = get(id, user);
		if (request.mileageKm() < vehicle.getMileageKm()) {
			throw new BusinessRuleException("MILEAGE_DECREASED", "A quilometragem não pode ser menor que a atual ("
					+ vehicle.getMileageKm() + " km).");
		}
		vehicle.update(request.model().trim(), request.modelYear(), request.color().trim(), request.mileageKm());
		return vehicle;
	}

	@Transactional
	public void delete(Long id, AuthenticatedUser user) {
		Vehicle vehicle = get(id, user);
		if (bookings.existsByVehicleIdAndStatusIn(id, BookingStatus.ACTIVE)) {
			throw new ConflictException("VEHICLE_HAS_ACTIVE_BOOKINGS",
					"O veículo tem agendamento em aberto. Cancele o agendamento antes de removê-lo.");
		}
		vehicles.delete(vehicle);
	}

	public HealthReport health(Long id, AuthenticatedUser user) {
		return healthCalculator.report(get(id, user));
	}

	public ServiceQuote quote(Long id, AuthenticatedUser user) {
		return quoteCalculator.quote(get(id, user));
	}

	/** Grava a leitura que a integração com os módulos do veículo enviou. */
	@Transactional
	public VehicleComponent recordReading(Long id, ComponentType type, ComponentReadingRequest request,
			AuthenticatedUser user) {
		Vehicle vehicle = get(id, user);
		VehicleComponent component = vehicle.component(type)
				.orElseThrow(() -> new NotFoundException("Componente", type));
		component.record(request.health(), request.detail(), clock.instant());
		return component;
	}

	static String normalizePlate(String plate) {
		return plate.replace("-", "").trim().toUpperCase(Locale.ROOT);
	}
}
