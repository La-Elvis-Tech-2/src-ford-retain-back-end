package com.fordretain.api.model.enums;

public enum ComponentType {
	OIL(VehicleSystem.ENGINE, "Óleo e filtro", "Troca de óleo sintético 5W30 e filtro de óleo", 520_00, 100_00, 30),
	AIR_FILTER(VehicleSystem.ENGINE, "Filtro de ar", "Troca do filtro de ar do motor", 165_00, 35_00, 10),
	CABIN_FILTER(VehicleSystem.ENGINE, "Filtro de cabine", "Troca do filtro de cabine (antipólen)", 140_00, 35_00, 10),
	SPARK_PLUGS(VehicleSystem.ENGINE, "Velas", "Troca das velas de ignição", 380_00, 90_00, 30),
	TIMING_BELT(VehicleSystem.ENGINE, "Correia", "Troca da correia dentada", 950_00, 350_00, 120),
	COOLANT(VehicleSystem.ENGINE, "Arrefecimento", "Troca do fluido de arrefecimento", 210_00, 80_00, 30),
	BRAKE_PAD(VehicleSystem.BRAKES, "Pastilha de freio", "Substituição das pastilhas de freio dianteiras", 480_00, 120_00, 40),
	BRAKE_FLUID(VehicleSystem.BRAKES, "Fluido de freio", "Troca do fluido de freio", 120_00, 70_00, 30),
	BATTERY(VehicleSystem.BATTERY, "Bateria", "Teste de carga da bateria (troca só se reprovar)", 0, 0, 0),
	TIRES(VehicleSystem.TIRES, "Pneus e alinhamento", "Alinhamento e balanceamento", 0, 180_00, 60);

	private final VehicleSystem system;
	private final String label;
	private final String service;
	private final long partsCents;
	private final long laborCents;
	private final int serviceMinutes;

	ComponentType(VehicleSystem system, String label, String service, long partsCents, long laborCents,
			int serviceMinutes) {
		this.system = system;
		this.label = label;
		this.service = service;
		this.partsCents = partsCents;
		this.laborCents = laborCents;
		this.serviceMinutes = serviceMinutes;
	}

	public VehicleSystem system() {
		return system;
	}

	public String label() {
		return label;
	}

	public String service() {
		return service;
	}

	public long partsCents() {
		return partsCents;
	}

	public long laborCents() {
		return laborCents;
	}

	public int serviceMinutes() {
		return serviceMinutes;
	}
}
