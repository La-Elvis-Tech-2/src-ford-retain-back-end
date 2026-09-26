package com.fordretain.api.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fordretain.api.model.Booking;
import com.fordretain.api.model.enums.BookingStatus;

/** As listagens trazem horário, concessionária e veículo na mesma consulta, sem N+1. */
public interface BookingRepository extends JpaRepository<Booking, Long> {

	@EntityGraph(attributePaths = { "slot", "slot.dealer", "vehicle" })
	List<Booking> findByCustomerIdOrderBySlotStartsAtAsc(Long customerId);

	@EntityGraph(attributePaths = { "slot", "slot.dealer", "vehicle" })
	List<Booking> findBySlotDealerIdOrderBySlotStartsAtAsc(Long dealerId);

	@EntityGraph(attributePaths = { "slot", "slot.dealer", "vehicle" })
	List<Booking> findAllByOrderBySlotStartsAtAsc();

	boolean existsByVehicleIdAndStatusIn(Long vehicleId, Collection<BookingStatus> statuses);

	boolean existsBySlotDealerId(Long dealerId);
}
