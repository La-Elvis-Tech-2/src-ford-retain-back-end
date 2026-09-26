package com.fordretain.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fordretain.api.model.Dealer;

public interface DealerRepository extends JpaRepository<Dealer, Long> {

	List<Dealer> findAllByOrderByNameAsc();
}
