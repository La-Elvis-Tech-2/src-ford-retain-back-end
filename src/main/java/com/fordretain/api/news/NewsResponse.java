package com.fordretain.api.news;

import java.time.Instant;

public record NewsResponse(Long id, NewsCategory category, String title, String summary, String body,
		Instant publishedAt) {

	public static NewsResponse from(News news) {
		return new NewsResponse(news.getId(), news.getCategory(), news.getTitle(), news.getSummary(), news.getBody(),
				news.getPublishedAt());
	}
}
