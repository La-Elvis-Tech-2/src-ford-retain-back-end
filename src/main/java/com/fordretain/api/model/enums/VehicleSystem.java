package com.fordretain.api.model.enums;


/** Os quatro sistemas do laudo. Cada componente pertence a exatamente um. */
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
