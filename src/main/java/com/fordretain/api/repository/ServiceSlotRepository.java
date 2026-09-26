package com.fordretain.api.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fordretain.api.model.ServiceSlot;

public interface ServiceSlotRepository extends JpaRepository<ServiceSlot, Long> {

	List<ServiceSlot> findByDealerIdAndAvailableTrueAndStartsAtAfterOrderByStartsAtAsc(Long dealerId, Instant after);

	boolean existsByDealerIdAndStartsAt(Long dealerId, Instant startsAt);
}
