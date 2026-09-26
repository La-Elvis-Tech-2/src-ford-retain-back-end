package com.fordretain.api.vehicle.health;

/** Status de um componente ou sistema, do mais tranquilo ao mais grave. */
public enum HealthStatus {
	OK,
	ATTENTION,
	URGENT;

	public boolean isWorseThan(HealthStatus other) {
		return ordinal() > other.ordinal();
	}
}
