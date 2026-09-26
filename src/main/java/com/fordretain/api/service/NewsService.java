package com.fordretain.api.service;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fordretain.api.dto.request.NewsRequest;
import com.fordretain.api.exception.NotFoundException;
import com.fordretain.api.model.News;
import com.fordretain.api.model.enums.NewsCategory;
import com.fordretain.api.repository.NewsRepository;

@Service
@Transactional(readOnly = true)
public class NewsService {

	private final NewsRepository news;
	private final Clock clock;

	public NewsService(NewsRepository news, Clock clock) {
		this.news = news;
		this.clock = clock;
	}

	public List<News> list(NewsCategory category) {
		return category == null ? news.findAllByOrderByPublishedAtDesc() : news.findByCategoryOrderByPublishedAtDesc(category);
	}

	public News get(Long id) {
		return news.findById(id).orElseThrow(() -> new NotFoundException("Novidade", id));
	}

	@Transactional
	public News create(NewsRequest request) {
		return news.save(new News(request.category(), request.title().trim(), request.summary().trim(),
				request.body().trim(), clock.instant()));
	}

	@Transactional
	public News update(Long id, NewsRequest request) {
		News item = get(id);
		item.update(request.category(), request.title().trim(), request.summary().trim(), request.body().trim());
		return item;
	}

	@Transactional
	public void delete(Long id) {
		news.delete(get(id));
	}
}
