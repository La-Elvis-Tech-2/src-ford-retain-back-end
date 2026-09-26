package com.fordretain.api.model;

import java.time.Instant;

import com.fordretain.api.model.enums.ComponentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A leitura atual de um componente. {@code health} vai de 0 a 100 e é a nota
 * dada pela leitura dos módulos; {@code healthLastWeek} é a mesma nota sete
 * dias antes, de onde sai a variação do laudo.
 */
@Entity
@Table(name = "vehicle_components")
public class VehicleComponent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ComponentType type;

	@Column(nullable = false)
	private int health;

	@Column(name = "health_last_week", nullable = false)
	private int healthLastWeek;

	@Column(length = 200)
	private String detail;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected VehicleComponent() {
	}

	VehicleComponent(Vehicle vehicle, ComponentType type, int health, int healthLastWeek, String detail,
			Instant updatedAt) {
		this.vehicle = vehicle;
		this.type = type;
		this.health = health;
		this.healthLastWeek = healthLastWeek;
		this.detail = detail;
		this.updatedAt = updatedAt;
	}

	/** Registra uma nova leitura; a referência da semana anterior não muda. */
	public void record(int health, String detail, Instant readAt) {
		this.health = health;
		this.detail = detail;
		this.updatedAt = readAt;
	}

	public Long getId() {
		return id;
	}

	public ComponentType getType() {
		return type;
	}

	public int getHealth() {
		return health;
	}

	public int getHealthLastWeek() {
		return healthLastWeek;
	}

	public String getDetail() {
		return detail;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
