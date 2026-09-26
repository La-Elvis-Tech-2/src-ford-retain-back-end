package com.fordretain.api.user;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Contas e perfis de acesso")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

	private final UserService service;

	public UserController(UserService service) {
		this.service = service;
	}

	@GetMapping("/me")
	@Operation(summary = "Dados da conta autenticada")
	public UserResponse me(AuthenticatedUser user) {
		return UserResponse.from(service.get(user.id(), user));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta uma conta", description = "O administrador consulta qualquer conta; os demais perfis, só a própria.")
	public UserResponse get(@PathVariable Long id, AuthenticatedUser user) {
		return UserResponse.from(service.get(id, user));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Lista todas as contas")
	public List<UserResponse> list() {
		return service.list().stream().map(UserResponse::from).toList();
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Cria uma conta com qualquer perfil")
	@ApiResponse(responseCode = "201", description = "Conta criada")
	@ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
	@ApiResponse(responseCode = "422", description = "Perfil DEALER sem concessionária, ou concessionária em outro perfil")
	public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
		User user = service.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
		return ResponseEntity.created(location).body(UserResponse.from(user));
	}
}
