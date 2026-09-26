package com.fordretain.api.dealer;

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

import com.fordretain.api.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/dealers")
@Tag(name = "Concessionárias", description = "Rede de concessionárias e horários da oficina")
public class DealerController {

	private final DealerService service;

	public DealerController(DealerService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Lista as concessionárias", description = "Endpoint público.")
	public List<DealerResponse> list() {
		return service.list().stream().map(DealerResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta uma concessionária", description = "Endpoint público.")
	public DealerResponse get(@PathVariable Long id) {
		return DealerResponse.from(service.get(id));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Cadastra uma concessionária")
	@ApiResponse(responseCode = "201", description = "Concessionária cadastrada")
	public ResponseEntity<DealerResponse> create(@Valid @RequestBody DealerRequest request) {
		Dealer dealer = service.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(dealer.getId()).toUri();
		return ResponseEntity.created(location).body(DealerResponse.from(dealer));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Atualiza os dados de uma concessionária")
	public DealerResponse update(@PathVariable Long id, @Valid @RequestBody DealerRequest request) {
		return DealerResponse.from(service.update(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Remove uma concessionária sem histórico")
	@ApiResponse(responseCode = "204", description = "Concessionária removida")
	@ApiResponse(responseCode = "409", description = "Concessionária com agendamentos ou atendentes")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/slots")
	@Operation(summary = "Horários livres da oficina", description = "Endpoint público. Só horários futuros e ainda não reservados.")
	public List<SlotResponse> slots(@PathVariable Long id) {
		return service.availableSlots(id).stream().map(SlotResponse::from).toList();
	}

	@GetMapping("/{id}/slots/{slotId}")
	@Operation(summary = "Consulta um horário", description = "Endpoint público. Mostra também horários já reservados.")
	public SlotResponse slot(@PathVariable Long id, @PathVariable Long slotId) {
		return SlotResponse.from(service.getSlot(id, slotId));
	}

	@PostMapping("/{id}/slots")
	@PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Abre um horário na oficina", description = "O atendente só abre horários na própria concessionária.")
	@ApiResponse(responseCode = "201", description = "Horário criado")
	@ApiResponse(responseCode = "409", description = "Já existe um horário neste início")
	@ApiResponse(responseCode = "422", description = "Horário no passado")
	public ResponseEntity<SlotResponse> createSlot(@PathVariable Long id, @Valid @RequestBody SlotRequest request,
			AuthenticatedUser user) {
		ServiceSlot slot = service.createSlot(id, request, user);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{slotId}").buildAndExpand(slot.getId()).toUri();
		return ResponseEntity.created(location).body(SlotResponse.from(slot));
	}
}
