package com.fordretain.api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fordretain.api.dto.request.BookingRequest;
import com.fordretain.api.dto.request.BookingStatusRequest;
import com.fordretain.api.dto.response.BookingResponse;
import com.fordretain.api.model.enums.BookingStatus;
import com.fordretain.api.security.AuthenticatedUser;
import com.fordretain.api.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/bookings")
@Tag(name = "Agendamentos", description = "Revisões agendadas na rede")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

	private final BookingService service;

	public BookingController(BookingService service) {
		this.service = service;
	}

	@PostMapping
	@PreAuthorize("hasRole('CUSTOMER')")
	@Operation(summary = "Agenda a revisão de um veículo", description = "Reserva o horário e fixa o valor da revisão recomendada.")
	@ApiResponse(responseCode = "201", description = "Revisão agendada")
	@ApiResponse(responseCode = "409", description = "Horário já reservado, ou veículo com revisão em aberto")
	@ApiResponse(responseCode = "422", description = "Horário no passado")
	public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request, AuthenticatedUser user) {
		BookingResponse booking = service.create(request, user);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(booking.id()).toUri();
		return ResponseEntity.created(location).body(booking);
	}

	@GetMapping
	@Operation(summary = "Lista os agendamentos", description = "Cliente: os próprios. Atendente: os da concessionária. Administrador: todos.")
	public List<BookingResponse> list(AuthenticatedUser user,
			@Parameter(description = "Filtra por status") @RequestParam(required = false) BookingStatus status) {
		return service.list(user, status);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta um agendamento")
	public BookingResponse get(@PathVariable Long id, AuthenticatedUser user) {
		return service.get(id, user);
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Muda o status do agendamento", description = "Cliente: só CANCELLED. Atendente e administrador: CONFIRMED, COMPLETED e CANCELLED.")
	@ApiResponse(responseCode = "200", description = "Status alterado")
	@ApiResponse(responseCode = "403", description = "Cliente tentando confirmar ou concluir")
	@ApiResponse(responseCode = "409", description = "Transição inválida para o status atual")
	public BookingResponse changeStatus(@PathVariable Long id, @Valid @RequestBody BookingStatusRequest request,
			AuthenticatedUser user) {
		return service.changeStatus(id, request.status(), user);
	}
}
