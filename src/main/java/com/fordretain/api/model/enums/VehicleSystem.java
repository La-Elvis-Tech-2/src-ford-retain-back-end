package com.fordretain.api.model.enums;

public enum VehicleSystem {
	ENGINE("Motor"),
	BRAKES("Freios"),
	BATTERY("Bateria"),
	TIRES("Pneus");

	private final String label;

	VehicleSystem(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}
