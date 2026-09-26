package com.fordretain.api.dealer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DealerRepository extends JpaRepository<Dealer, Long> {

	List<Dealer> findAllByOrderByNameAsc();
}
