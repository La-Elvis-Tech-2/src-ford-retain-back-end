package com.fordretain.api.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.fordretain.api.support.IntegrationTest;

@DisplayName("Usuários e perfis")
class UserIntegrationTest extends IntegrationTest {

	@Test
	@DisplayName("Conta autenticada consulta os próprios dados em /users/me")
	void me() throws Exception {
		mvc.perform(get("/users/me").with(bearer(customerToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(CUSTOMER))
				.andExpect(jsonPath("$.role").value("CUSTOMER"));
	}

	@Test
	@DisplayName("Administrador lista todas as contas")
	void adminListsUsers() throws Exception {
		mvc.perform(get("/users").with(bearer(adminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))));
	}

	@Test
	@DisplayName("Cliente consulta a própria conta, mas não a de outra pessoa (403)")
	void customerSeesOnlyOwnAccount() throws Exception {
		Customer customer = newCustomer();

		mvc.perform(get("/users/{id}", customer.id()).with(bearer(customer.token())))
				.andExpect(status().isOk());
		mvc.perform(get("/users/{id}", 1).with(bearer(customer.token())))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Administrador cria atendente vinculado a uma concessionária")
	void adminCreatesDealerUser() throws Exception {
		mvc.perform(post("/users").with(bearer(adminToken())).contentType(MediaType.APPLICATION_JSON)
				.content(json("name", "Atendimento Vila Prudente", "email", "vp-" + UUID.randomUUID() + "@fordretain.com",
						"password", "Concessionaria@123", "role", "DEALER", "dealerId", VILA_PRUDENTE_ID)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.role").value("DEALER"))
				.andExpect(jsonPath("$.dealerId").value(VILA_PRUDENTE_ID));
	}

	@Test
	@DisplayName("Atendente sem concessionária responde 422")
	void dealerRequiresDealership() throws Exception {
		mvc.perform(post("/users").with(bearer(adminToken())).contentType(MediaType.APPLICATION_JSON)
				.content(json("name", "Sem Loja", "email", "sem-loja-" + UUID.randomUUID() + "@fordretain.com",
						"password", "Concessionaria@123", "role", "DEALER")))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.code").value("DEALER_REQUIRED"));
	}

	@Test
	@DisplayName("Atendente não cria contas (403)")
	void dealerCannotCreateUsers() throws Exception {
		mvc.perform(post("/users").with(bearer(dealerToken())).contentType(MediaType.APPLICATION_JSON)
				.content(json("name", "Novo Admin", "email", "admin2@fordretain.com", "password", "Admin@12345", "role", "ADMIN")))
				.andExpect(status().isForbidden());
	}
}
