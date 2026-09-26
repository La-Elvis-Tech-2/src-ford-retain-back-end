package com.fordretain.api.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fordretain.api.model.enums.ComponentType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicles")
public class Vehicle {

	static final String AWAITING_READING = "Aguardando a primeira leitura dos módulos.";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@Column(nullable = false, length = 7, unique = true)
	private String plate;

	@Column(nullable = false, length = 80)
	private String model;

	@Column(name = "model_year", nullable = false)
	private int modelYear;

	@Column(nullable = false, length = 40)
	private String color;

	@Column(name = "mileage_km", nullable = false)
	private int mileageKm;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private List<VehicleComponent> components = new ArrayList<>();

	protected Vehicle() {
	}

	public Vehicle(User owner, String plate, String model, int modelYear, String color, int mileageKm,
			Instant createdAt) {
		this.owner = owner;
		this.plate = plate;
		this.model = model;
		this.modelYear = modelYear;
		this.color = color;
		this.mileageKm = mileageKm;
		this.createdAt = createdAt;
	}

	public void initializeComponents(Instant now) {
		for (ComponentType type : ComponentType.values()) {
			components.add(new VehicleComponent(this, type, 100, 100, AWAITING_READING, now));
		}
	}

	public void addReading(ComponentType type, int health, int healthLastWeek, String detail, Instant readAt) {
		components.add(new VehicleComponent(this, type, health, healthLastWeek, detail, readAt));
	}

	public void update(String model, int modelYear, String color, int mileageKm) {
		this.model = model;
		this.modelYear = modelYear;
		this.color = color;
		this.mileageKm = mileageKm;
	}

	public Optional<VehicleComponent> component(ComponentType type) {
		return components.stream().filter(component -> component.getType() == type).findFirst();
	}

	public boolean isOwnedBy(Long userId) {
		return owner.getId().equals(userId);
	}

	public Long getId() {
		return id;
	}

	public User getOwner() {
		return owner;
	}

	public String getPlate() {
		return plate;
	}

	public String getModel() {
		return model;
	}

	public int getModelYear() {
		return modelYear;
	}

	public String getColor() {
		return color;
	}

	public int getMileageKm() {
		return mileageKm;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public List<VehicleComponent> getComponents() {
		return components;
	}
}
