package com.fordretain.api.model.enums;

import java.util.EnumSet;
import java.util.Set;

public enum BookingStatus {
	REQUESTED,
	CONFIRMED,
	COMPLETED,
	CANCELLED;

	public static final Set<BookingStatus> ACTIVE = EnumSet.of(REQUESTED, CONFIRMED);

	public boolean canMoveTo(BookingStatus next) {
		return switch (this) {
			case REQUESTED -> next == CONFIRMED || next == CANCELLED;
			case CONFIRMED -> next == COMPLETED || next == CANCELLED;
			case COMPLETED, CANCELLED -> false;
		};
	}
}
