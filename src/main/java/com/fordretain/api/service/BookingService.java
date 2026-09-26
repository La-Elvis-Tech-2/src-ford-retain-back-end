package com.fordretain.api.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fordretain.api.component.QuoteCalculator;
import com.fordretain.api.dto.request.BookingRequest;
import com.fordretain.api.dto.response.BookingResponse;
import com.fordretain.api.exception.BusinessRuleException;
import com.fordretain.api.exception.ConflictException;
import com.fordretain.api.exception.NotFoundException;
import com.fordretain.api.model.Booking;
import com.fordretain.api.model.ServiceSlot;
import com.fordretain.api.model.Vehicle;
import com.fordretain.api.model.enums.BookingStatus;
import com.fordretain.api.model.enums.Role;
import com.fordretain.api.repository.BookingRepository;
import com.fordretain.api.repository.ServiceSlotRepository;
import com.fordretain.api.repository.UserRepository;
import com.fordretain.api.security.AuthenticatedUser;

/**
 * Agendamentos de revisão.
 *
 * Cada perfil enxerga um recorte: o cliente, os próprios; o atendente, os da
 * sua concessionária; o administrador, todos. Fora do recorte, o agendamento
 * responde 404.
 *
 * Quem pode mudar o status: o cliente só cancela; o atendente e o
 * administrador confirmam, concluem e cancelam. A transição também precisa
 * ser válida no ciclo de vida ({@link BookingStatus#canMoveTo}).
 *
 * As respostas são montadas dentro da transação: concessionária, veículo e
 * cliente são carregados sob demanda e não existem mais depois dela.
 */
@Service
@Transactional(readOnly = true)
public class BookingService {

	private final BookingRepository bookings;
	private final ServiceSlotRepository slots;
	private final VehicleService vehicles;
	private final UserRepository users;
	private final QuoteCalculator quoteCalculator;
	private final Clock clock;

	public BookingService(BookingRepository bookings, ServiceSlotRepository slots, VehicleService vehicles,
			UserRepository users, QuoteCalculator quoteCalculator, Clock clock) {
		this.bookings = bookings;
		this.slots = slots;
		this.vehicles = vehicles;
		this.users = users;
		this.quoteCalculator = quoteCalculator;
		this.clock = clock;
	}

	@Transactional
	public BookingResponse create(BookingRequest request, AuthenticatedUser user) {
		Vehicle vehicle = vehicles.get(request.vehicleId(), user);
		ServiceSlot slot = slots.findById(request.slotId())
				.orElseThrow(() -> new NotFoundException("Horário", request.slotId()));

		Instant now = clock.instant();
		if (!slot.getStartsAt().isAfter(now)) {
			throw new BusinessRuleException("SLOT_IN_PAST", "Este horário já passou. Escolha um horário futuro.");
		}
		if (!slot.isAvailable()) {
			throw new ConflictException("SLOT_UNAVAILABLE", "Este horário já foi reservado. Escolha outro.");
		}
		if (bookings.existsByVehicleIdAndStatusIn(vehicle.getId(), BookingStatus.ACTIVE)) {
			throw new ConflictException("VEHICLE_ALREADY_BOOKED",
					"O veículo já tem uma revisão agendada. Cancele-a para marcar outra.");
		}

		slot.reserve();
		long total = quoteCalculator.quote(vehicle).totalCents();
		Booking booking = new Booking(users.getReferenceById(user.id()), vehicle, slot, BookingStatus.REQUESTED, total, now);
		return BookingResponse.from(bookings.save(booking));
	}

	public List<BookingResponse> list(AuthenticatedUser user, BookingStatus status) {
		List<Booking> visible = switch (user.role()) {
			case ADMIN -> bookings.findAllByOrderBySlotStartsAtAsc();
			case DEALER -> bookings.findBySlotDealerIdOrderBySlotStartsAtAsc(user.dealerId());
			case CUSTOMER -> bookings.findByCustomerIdOrderBySlotStartsAtAsc(user.id());
		};
		return visible.stream()
				.filter(booking -> status == null || booking.getStatus() == status)
				.map(BookingResponse::from)
				.toList();
	}

	public BookingResponse get(Long id, AuthenticatedUser user) {
		return BookingResponse.from(find(id, user));
	}

	@Transactional
	public BookingResponse changeStatus(Long id, BookingStatus next, AuthenticatedUser user) {
		Booking booking = find(id, user);

		if (user.is(Role.CUSTOMER) && next != BookingStatus.CANCELLED) {
			throw new AccessDeniedException("O cliente só pode cancelar o agendamento.");
		}
		if (!booking.getStatus().canMoveTo(next)) {
			throw new ConflictException("INVALID_STATUS_TRANSITION",
					"O agendamento está " + booking.getStatus() + " e não pode passar para " + next + ".");
		}

		booking.moveTo(next, clock.instant());
		if (next == BookingStatus.CANCELLED) {
			booking.getSlot().release();
		}
		return BookingResponse.from(booking);
	}

	private Booking find(Long id, AuthenticatedUser user) {
		return bookings.findById(id)
				.filter(booking -> isVisibleTo(booking, user))
				.orElseThrow(() -> new NotFoundException("Agendamento", id));
	}

	private static boolean isVisibleTo(Booking booking, AuthenticatedUser user) {
		return switch (user.role()) {
			case ADMIN -> true;
			case DEALER -> booking.getSlot().getDealer().getId().equals(user.dealerId());
			case CUSTOMER -> booking.getCustomer().getId().equals(user.id());
		};
	}
}
