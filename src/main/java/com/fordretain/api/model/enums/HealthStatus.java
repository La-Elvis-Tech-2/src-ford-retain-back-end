package com.fordretain.api.model.enums;

public enum HealthStatus {
	OK,
	ATTENTION,
	URGENT;

	static final int URGENT_BELOW = 40;
	static final int ATTENTION_BELOW = 75;

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
