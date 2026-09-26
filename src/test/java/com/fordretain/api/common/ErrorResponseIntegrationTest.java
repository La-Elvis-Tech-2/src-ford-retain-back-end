package com.fordretain.api.common;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.fordretain.api.support.IntegrationTest;

@DisplayName("Padrão das respostas de erro")
class ErrorResponseIntegrationTest extends IntegrationTest {

	@Test
	@DisplayName("Todo erro traz status, título, detalhe, código, instância e data no formato problem+json")
	void problemDetailShape() throws Exception {
		mvc.perform(get("/vehicles/{id}", 9_999).with(bearer(adminToken())))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Não encontrado"))
				.andExpect(jsonPath("$.detail").value("Veículo 9999 não encontrado."))
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
				.andExpect(jsonPath("$.instance").value("/vehicles/9999"))
				.andExpect(jsonPath("$.timestamp", notNullValue()));
	}

	@Test
	@DisplayName("JSON malformado responde 400 com o código MALFORMED_REQUEST")
	void malformedJson() throws Exception {
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{ email: "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
	}

	@Test
	@DisplayName("Id com tipo errado na rota responde 400")
	void pathVariableTypeMismatch() throws Exception {
		mvc.perform(get("/dealers/{id}", "abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
	}

	@Test
	@DisplayName("Método não suportado no recurso responde 405")
	void methodNotAllowed() throws Exception {
		mvc.perform(delete("/news").with(bearer(adminToken())))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
	}

	@Test
	@DisplayName("Rota inexistente responde 404 no mesmo formato")
	void unknownRoute() throws Exception {
		mvc.perform(get("/rota-que-nao-existe").with(bearer(adminToken())))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	@DisplayName("Content-Type diferente de JSON responde 415")
	void unsupportedMediaType() throws Exception {
		mvc.perform(post("/auth/login").contentType(MediaType.TEXT_PLAIN).content("email=x"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
	}
}
