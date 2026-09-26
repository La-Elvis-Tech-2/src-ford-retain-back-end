package com.fordretain.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.fordretain.api.support.IntegrationTest;

@DisplayName("Autenticação: cadastro e login")
class AuthIntegrationTest extends IntegrationTest {

	@Autowired
	private JwtDecoder jwtDecoder;

	@Test
	@DisplayName("Cadastro público cria um cliente e responde 201 com Location")
	void registerCreatesCustomer() throws Exception {
		String email = "novo-" + UUID.randomUUID() + "@teste.com";

		mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(json("name", "Maria Oliveira", "email", email, "password", "Senha@2026")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", matchesPattern(".*/users/\\d+$")))
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.role").value("CUSTOMER"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	@DisplayName("Cadastro com e-mail já existente responde 409")
	void registerDuplicateEmail() throws Exception {
		mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(json("name", "Outro Vitor", "email", "CLIENTE@fordretain.com", "password", "Senha@2026")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
	}

	@Test
	@DisplayName("Cadastro inválido responde 400 com a lista de campos rejeitados")
	void registerInvalidPayload() throws Exception {
		mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(json("name", "", "email", "nao-e-email", "password", "123")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("name", "email", "password")));
	}

	@Test
	@DisplayName("Login válido emite JWT com sub, perfil, emissor e validade de 1 hora")
	void loginIssuesToken() throws Exception {
		String token = login(CUSTOMER, CUSTOMER_PASSWORD);

		Jwt jwt = jwtDecoder.decode(token);
		assertThat(jwt.getSubject()).isEqualTo("4");
		assertThat(jwt.getClaimAsStringList("roles")).containsExactly("CUSTOMER");
		assertThat(jwt.getClaimAsString("iss")).isEqualTo("ford-retain-api");
		assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(1));
		assertThat(jwt.getId()).isNotBlank();
	}

	@Test
	@DisplayName("Token de atendente carrega a concessionária em dealerId")
	void dealerTokenCarriesDealer() throws Exception {
		Jwt jwt = jwtDecoder.decode(dealerToken());

		assertThat(jwt.getClaimAsStringList("roles")).containsExactly("DEALER");
		assertThat(((Number) jwt.getClaim("dealerId")).longValue()).isEqualTo(TATUAPE_ID);
	}

	@Test
	@DisplayName("Resposta do login informa tipo e validade do token")
	void loginResponseShape() throws Exception {
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json("email", ADMIN, "password", ADMIN_PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(3600))
				.andExpect(jsonPath("$.user.role").value("ADMIN"));
	}

	@Test
	@DisplayName("Senha errada responde 401 sem emitir token")
	void loginWrongPassword() throws Exception {
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json("email", CUSTOMER, "password", "senha-errada")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
				.andExpect(jsonPath("$.accessToken").doesNotExist());
	}

	@Test
	@DisplayName("E-mail inexistente recebe a mesma resposta da senha errada")
	void loginUnknownEmail() throws Exception {
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json("email", "ninguem@teste.com", "password", "qualquer-senha")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
				.andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
	}

	@Test
	@DisplayName("Login sem senha responde 400")
	void loginMissingPassword() throws Exception {
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(json("email", CUSTOMER)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[*].field", hasItem("password")));
	}
}
