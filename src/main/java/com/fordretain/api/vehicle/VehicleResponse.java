package com.fordretain.api.vehicle;

import java.time.Instant;

public record VehicleResponse(Long id, Long ownerId, String plate, String model, int modelYear, String color,
		int mileageKm, Instant createdAt) {

	public static VehicleResponse from(Vehicle vehicle) {
		return new VehicleResponse(vehicle.getId(), vehicle.getOwner().getId(), vehicle.getPlate(), vehicle.getModel(),
				vehicle.getModelYear(), vehicle.getColor(), vehicle.getMileageKm(), vehicle.getCreatedAt());
	}
}
