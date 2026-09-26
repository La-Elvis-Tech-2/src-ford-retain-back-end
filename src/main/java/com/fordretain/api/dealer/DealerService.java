package com.fordretain.api.dealer;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fordretain.api.booking.BookingRepository;
import com.fordretain.api.common.error.BusinessRuleException;
import com.fordretain.api.common.error.ConflictException;
import com.fordretain.api.common.error.NotFoundException;
import com.fordretain.api.security.AuthenticatedUser;
import com.fordretain.api.user.Role;
import com.fordretain.api.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class DealerService {

	private final DealerRepository dealers;
	private final ServiceSlotRepository slots;
	private final BookingRepository bookings;
	private final UserRepository users;
	private final Clock clock;

	public DealerService(DealerRepository dealers, ServiceSlotRepository slots, BookingRepository bookings,
			UserRepository users, Clock clock) {
		this.dealers = dealers;
		this.slots = slots;
		this.bookings = bookings;
		this.users = users;
		this.clock = clock;
	}

	public List<Dealer> list() {
		return dealers.findAllByOrderByNameAsc();
	}

	public Dealer get(Long id) {
		return dealers.findById(id).orElseThrow(() -> new NotFoundException("Concessionária", id));
	}

	@Transactional
	public Dealer create(DealerRequest request) {
		Dealer dealer = new Dealer(request.name().trim(), request.address().trim(), request.district().trim(),
				request.rating(), request.reviewCount(), clock.instant());
		return dealers.save(dealer);
	}

	@Transactional
	public Dealer update(Long id, DealerRequest request) {
		Dealer dealer = get(id);
		dealer.update(request.name().trim(), request.address().trim(), request.district().trim(), request.rating(),
				request.reviewCount());
		return dealer;
	}

	/** Só sai da rede quem não tem histórico: agendamentos e atendentes seguram a exclusão. */
	@Transactional
	public void delete(Long id) {
		Dealer dealer = get(id);
		if (bookings.existsBySlotDealerId(id)) {
			throw new ConflictException("DEALER_HAS_BOOKINGS", "A concessionária tem agendamentos e não pode ser excluída.");
		}
		if (users.existsByDealerId(id)) {
			throw new ConflictException("DEALER_HAS_USERS", "A concessionária tem atendentes vinculados e não pode ser excluída.");
		}
		dealers.delete(dealer);
	}

	/** Os horários livres daqui para a frente, do mais próximo ao mais distante. */
	public List<ServiceSlot> availableSlots(Long dealerId) {
		get(dealerId);
		return slots.findByDealerIdAndAvailableTrueAndStartsAtAfterOrderByStartsAtAsc(dealerId, clock.instant());
	}

	public ServiceSlot getSlot(Long dealerId, Long slotId) {
		return slots.findById(slotId)
				.filter(slot -> slot.getDealer().getId().equals(dealerId))
				.orElseThrow(() -> new NotFoundException("Horário", slotId));
	}

	@Transactional
	public ServiceSlot createSlot(Long dealerId, SlotRequest request, AuthenticatedUser user) {
		Dealer dealer = get(dealerId);
		if (user.is(Role.DEALER) && !dealerId.equals(user.dealerId())) {
			throw new AccessDeniedException("O atendente só abre horários na própria concessionária.");
		}
		Instant startsAt = request.startsAt();
		if (!startsAt.isAfter(clock.instant())) {
			throw new BusinessRuleException("SLOT_IN_PAST", "O horário precisa estar no futuro.");
		}
		if (slots.existsByDealerIdAndStartsAt(dealerId, startsAt)) {
			throw new ConflictException("SLOT_ALREADY_EXISTS", "Já existe um horário neste início.");
		}
		return slots.save(new ServiceSlot(dealer, startsAt));
	}
}
