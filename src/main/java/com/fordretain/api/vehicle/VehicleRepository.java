package com.fordretain.api.vehicle;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

	List<Vehicle> findByOwnerIdOrderByIdAsc(Long ownerId);

	List<Vehicle> findAllByOrderByIdAsc();

	boolean existsByPlate(String plate);
}
