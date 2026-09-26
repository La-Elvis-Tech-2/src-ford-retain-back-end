package com.fordretain.api.dto.response;

import java.time.Instant;

import com.fordretain.api.model.Booking;
import com.fordretain.api.model.enums.BookingStatus;

public record BookingResponse(
		Long id,
		BookingStatus status,
		Instant startsAt,
		long totalCents,
		VehicleSummary vehicle,
		DealerSummary dealer,
		Long customerId,
		Instant createdAt,
		Instant updatedAt) {

	public record VehicleSummary(Long id, String plate, String model) {
	}

	public record DealerSummary(Long id, String name, String address) {
	}

	public static BookingResponse from(Booking booking) {
		var vehicle = booking.getVehicle();
		var dealer = booking.getSlot().getDealer();
		return new BookingResponse(booking.getId(), booking.getStatus(), booking.getSlot().getStartsAt(),
				booking.getTotalCents(), new VehicleSummary(vehicle.getId(), vehicle.getPlate(), vehicle.getModel()),
				new DealerSummary(dealer.getId(), dealer.getName(), dealer.getAddress()),
				booking.getCustomer().getId(), booking.getCreatedAt(), booking.getUpdatedAt());
	}
}
