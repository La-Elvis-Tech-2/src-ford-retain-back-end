package com.fordretain.api.model;

import java.time.Instant;

import com.fordretain.api.model.enums.NewsCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "news")
public class News {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NewsCategory category;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(nullable = false, length = 300)
	private String summary;

	@Column(nullable = false, length = 4000)
	private String body;

	@Column(name = "published_at", nullable = false)
	private Instant publishedAt;

	protected News() {
	}

	public News(NewsCategory category, String title, String summary, String body, Instant publishedAt) {
		update(category, title, summary, body);
		this.publishedAt = publishedAt;
	}

	public void update(NewsCategory category, String title, String summary, String body) {
		this.category = category;
		this.title = title;
		this.summary = summary;
		this.body = body;
	}

	public Long getId() {
		return id;
	}

	public NewsCategory getCategory() {
		return category;
	}

	public String getTitle() {
		return title;
	}

	public String getSummary() {
		return summary;
	}

	public String getBody() {
		return body;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}
}
