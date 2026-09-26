package com.fordretain.api.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fordretain.api.dto.request.LoginRequest;
import com.fordretain.api.dto.request.RegisterRequest;
import com.fordretain.api.dto.response.TokenResponse;
import com.fordretain.api.dto.response.UserResponse;
import com.fordretain.api.model.User;
import com.fordretain.api.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Cadastro de clientes e emissão do token JWT")
public class AuthController {

	private final AuthService service;

	public AuthController(AuthService service) {
		this.service = service;
	}

	@PostMapping("/register")
	@Operation(summary = "Cadastra um cliente", description = "Endpoint público. A conta nasce com o perfil CUSTOMER.")
	@ApiResponse(responseCode = "201", description = "Cliente cadastrado")
	@ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		User user = service.register(request);
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/users/{id}")
				.buildAndExpand(user.getId()).toUri();
		return ResponseEntity.created(location).body(UserResponse.from(user));
	}

	@PostMapping("/login")
	@Operation(summary = "Autentica e emite o token de acesso", description = "Endpoint público. Devolve um JWT válido pelo tempo em expiresIn.")
	@ApiResponse(responseCode = "200", description = "Autenticado")
	@ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return service.login(request);
	}
}
