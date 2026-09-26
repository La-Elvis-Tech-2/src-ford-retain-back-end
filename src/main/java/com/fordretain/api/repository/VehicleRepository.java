package com.fordretain.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fordretain.api.model.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

	List<Vehicle> findByOwnerIdOrderByIdAsc(Long ownerId);

	List<Vehicle> findAllByOrderByIdAsc();

	boolean existsByPlate(String plate);
}
