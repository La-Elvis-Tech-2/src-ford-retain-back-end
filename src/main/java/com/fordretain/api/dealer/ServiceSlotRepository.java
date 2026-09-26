package com.fordretain.api.dealer;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceSlotRepository extends JpaRepository<ServiceSlot, Long> {

	List<ServiceSlot> findByDealerIdAndAvailableTrueAndStartsAtAfterOrderByStartsAtAsc(Long dealerId, Instant after);

	boolean existsByDealerIdAndStartsAt(Long dealerId, Instant startsAt);
}
