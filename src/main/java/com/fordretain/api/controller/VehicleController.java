package com.fordretain.api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fordretain.api.dto.request.ComponentReadingRequest;
import com.fordretain.api.dto.request.VehicleRequest;
import com.fordretain.api.dto.request.VehicleUpdateRequest;
import com.fordretain.api.dto.response.ComponentResponse;
import com.fordretain.api.dto.response.HealthReport;
import com.fordretain.api.dto.response.ServiceQuote;
import com.fordretain.api.dto.response.VehicleResponse;
import com.fordretain.api.model.Vehicle;
import com.fordretain.api.model.enums.ComponentType;
import com.fordretain.api.security.AuthenticatedUser;
import com.fordretain.api.service.VehicleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Veículos", description = "Veículos do cliente, laudo de saúde e revisão recomendada")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
public class VehicleController {

	private final VehicleService service;

	public VehicleController(VehicleService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Lista os veículos", description = "O cliente vê os próprios; o administrador vê todos.")
	public List<VehicleResponse> list(AuthenticatedUser user) {
		return service.list(user).stream().map(VehicleResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta um veículo")
	public VehicleResponse get(@PathVariable Long id, AuthenticatedUser user) {
		return VehicleResponse.from(service.get(id, user));
	}

	@PostMapping
	@PreAuthorize("hasRole('CUSTOMER')")
	@Operation(summary = "Cadastra um veículo do cliente", description = "Os componentes nascem aguardando a primeira leitura dos módulos.")
	@ApiResponse(responseCode = "201", description = "Veículo cadastrado")
	@ApiResponse(responseCode = "409", description = "Placa já cadastrada")
	public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request, AuthenticatedUser user) {
		Vehicle vehicle = service.create(request, user);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(vehicle.getId()).toUri();
		return ResponseEntity.created(location).body(VehicleResponse.from(vehicle));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualiza os dados do veículo")
	@ApiResponse(responseCode = "422", description = "Quilometragem menor que a atual")
	public VehicleResponse update(@PathVariable Long id, @Valid @RequestBody VehicleUpdateRequest request,
			AuthenticatedUser user) {
		return VehicleResponse.from(service.update(id, request, user));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Remove o veículo da conta")
	@ApiResponse(responseCode = "204", description = "Veículo removido")
	@ApiResponse(responseCode = "409", description = "Veículo com agendamento em aberto")
	public ResponseEntity<Void> delete(@PathVariable Long id, AuthenticatedUser user) {
		service.delete(id, user);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/health")
	@Operation(summary = "Laudo de saúde", description = "Nota geral, os quatro sistemas e cada componente com status e variação da semana.")
	public HealthReport health(@PathVariable Long id, AuthenticatedUser user) {
		return service.health(id, user);
	}

	@GetMapping("/{id}/service-quote")
	@Operation(summary = "Revisão recomendada", description = "Um item por componente fora do status em dia, com peças, mão de obra e duração.")
	public ServiceQuote quote(@PathVariable Long id, AuthenticatedUser user) {
		return service.quote(id, user);
	}

	@PutMapping("/{id}/components/{type}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Registra a leitura de um componente", description = "Usado pela integração com os módulos do veículo.")
	public ComponentResponse recordReading(@PathVariable Long id, @PathVariable ComponentType type,
			@Valid @RequestBody ComponentReadingRequest request, AuthenticatedUser user) {
		return ComponentResponse.from(service.recordReading(id, type, request, user));
	}
}
