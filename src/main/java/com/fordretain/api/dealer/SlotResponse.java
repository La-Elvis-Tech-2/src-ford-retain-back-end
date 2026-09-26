package com.fordretain.api.dealer;

import java.time.Instant;

public record SlotResponse(Long id, Long dealerId, Instant startsAt, boolean available) {

	public static SlotResponse from(ServiceSlot slot) {
		return new SlotResponse(slot.getId(), slot.getDealer().getId(), slot.getStartsAt(), slot.isAvailable());
	}
}
