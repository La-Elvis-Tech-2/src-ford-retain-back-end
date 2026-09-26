package com.fordretain.api.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dealers")
public class Dealer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, length = 200)
	private String address;

	@Column(nullable = false, length = 120)
	private String district;

	@Column(nullable = false, precision = 2, scale = 1)
	private BigDecimal rating;

	@Column(name = "review_count", nullable = false)
	private int reviewCount;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected Dealer() {
	}

	public Dealer(String name, String address, String district, BigDecimal rating, int reviewCount, Instant createdAt) {
		update(name, address, district, rating, reviewCount);
		this.createdAt = createdAt;
	}

	public void update(String name, String address, String district, BigDecimal rating, int reviewCount) {
		this.name = name;
		this.address = address;
		this.district = district;
		this.rating = rating;
		this.reviewCount = reviewCount;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getDistrict() {
		return district;
	}

	public BigDecimal getRating() {
		return rating;
	}

	public int getReviewCount() {
		return reviewCount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
