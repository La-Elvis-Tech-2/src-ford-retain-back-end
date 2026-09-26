package com.fordretain.api.news;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {

	List<News> findAllByOrderByPublishedAtDesc();

	List<News> findByCategoryOrderByPublishedAtDesc(NewsCategory category);
}
