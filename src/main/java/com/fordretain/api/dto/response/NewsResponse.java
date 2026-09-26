package com.fordretain.api.dto.response;

import java.time.Instant;

import com.fordretain.api.model.News;
import com.fordretain.api.model.enums.NewsCategory;

public record NewsResponse(Long id, NewsCategory category, String title, String summary, String body,
		Instant publishedAt) {

	public static NewsResponse from(News news) {
		return new NewsResponse(news.getId(), news.getCategory(), news.getTitle(), news.getSummary(), news.getBody(),
				news.getPublishedAt());
	}
}
