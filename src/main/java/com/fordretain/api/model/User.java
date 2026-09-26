package com.fordretain.api.model;

import java.time.Instant;

import com.fordretain.api.model.enums.Role;

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
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, length = 160, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	/** Só para {@link Role#DEALER}: a concessionária em que a pessoa atende. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dealer_id")
	private Dealer dealer;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected User() {
	}

	public User(String name, String email, String passwordHash, Role role, Dealer dealer, Instant createdAt) {
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
		this.dealer = dealer;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Role getRole() {
		return role;
	}

	public Dealer getDealer() {
		return dealer;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
