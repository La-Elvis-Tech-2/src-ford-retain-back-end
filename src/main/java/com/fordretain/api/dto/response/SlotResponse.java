package com.fordretain.api.dto.response;

import java.time.Instant;

import com.fordretain.api.model.ServiceSlot;

public record SlotResponse(Long id, Long dealerId, Instant startsAt, boolean available) {

	public static SlotResponse from(ServiceSlot slot) {
		return new SlotResponse(slot.getId(), slot.getDealer().getId(), slot.getStartsAt(), slot.isAvailable());
	}
}
