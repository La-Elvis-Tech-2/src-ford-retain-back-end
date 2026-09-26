package com.fordretain.api.vehicle.health;

/** Status de um componente ou sistema, do mais tranquilo ao mais grave. */
public enum HealthStatus {
	OK,
	ATTENTION,
	URGENT;

	static final int URGENT_BELOW = 40;
	static final int ATTENTION_BELOW = 75;

	/** Nota abaixo de 40: urgente; abaixo de 75: atenção; o resto está em dia. */
	public static HealthStatus of(int health) {
		if (health < URGENT_BELOW) {
			return URGENT;
		}
		return health < ATTENTION_BELOW ? ATTENTION : OK;
	}

	public boolean isWorseThan(HealthStatus other) {
		return ordinal() > other.ordinal();
	}
}
