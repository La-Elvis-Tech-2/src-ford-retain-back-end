package com.fordretain.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fordretain.api.model.News;
import com.fordretain.api.model.enums.NewsCategory;

public interface NewsRepository extends JpaRepository<News, Long> {

	List<News> findAllByOrderByPublishedAtDesc();

	List<News> findByCategoryOrderByPublishedAtDesc(NewsCategory category);
}
