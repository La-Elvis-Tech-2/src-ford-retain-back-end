package com.fordretain.api.model;

import java.time.Instant;

import com.fordretain.api.model.enums.BookingStatus;

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

@Entity
@Table(name = "bookings")
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private User customer;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "slot_id", nullable = false)
	private ServiceSlot slot;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BookingStatus status;

	@Column(name = "total_cents", nullable = false)
	private long totalCents;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Booking() {
	}

	public Booking(User customer, Vehicle vehicle, ServiceSlot slot, BookingStatus status, long totalCents,
			Instant createdAt) {
		this.customer = customer;
		this.vehicle = vehicle;
		this.slot = slot;
		this.status = status;
		this.totalCents = totalCents;
		this.createdAt = createdAt;
		this.updatedAt = createdAt;
	}

	public void moveTo(BookingStatus next, Instant now) {
		this.status = next;
		this.updatedAt = now;
	}

	public Long getId() {
		return id;
	}

	public User getCustomer() {
		return customer;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public ServiceSlot getSlot() {
		return slot;
	}

	public BookingStatus getStatus() {
		return status;
	}

	public long getTotalCents() {
		return totalCents;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
