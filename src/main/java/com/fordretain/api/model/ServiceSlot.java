package com.fordretain.api.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Um horário da oficina. {@code @Version} garante que duas reservas simultâneas
 * do mesmo horário não passem: a segunda falha com 409.
 */
@Entity
@Table(name = "service_slots")
public class ServiceSlot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "dealer_id", nullable = false)
	private Dealer dealer;

	@Column(name = "starts_at", nullable = false)
	private Instant startsAt;

	@Column(nullable = false)
	private boolean available;

	@Version
	@Column(nullable = false)
	private long version;

	protected ServiceSlot() {
	}

	public ServiceSlot(Dealer dealer, Instant startsAt) {
		this.dealer = dealer;
		this.startsAt = startsAt;
		this.available = true;
	}

	public void reserve() {
		this.available = false;
	}

	public void release() {
		this.available = true;
	}

	public Long getId() {
		return id;
	}

	public Dealer getDealer() {
		return dealer;
	}

	public Instant getStartsAt() {
		return startsAt;
	}

	public boolean isAvailable() {
		return available;
	}
}
