package com.fordretain.api.news;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.fordretain.api.support.IntegrationTest;

@DisplayName("Novidades da rede")
class NewsIntegrationTest extends IntegrationTest {

	@Test
	@DisplayName("Lista de novidades é pública, da mais recente para a mais antiga")
	void listIsPublic() throws Exception {
		mvc.perform(get("/news"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
	}

	@Test
	@DisplayName("Filtro por categoria devolve só a categoria pedida")
	void filterByCategory() throws Exception {
		mvc.perform(get("/news").param("category", "SAFETY"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].category", everyItem(is("SAFETY"))));
	}

	@Test
	@DisplayName("Categoria inexistente no filtro responde 400")
	void invalidCategory() throws Exception {
		mvc.perform(get("/news").param("category", "FOFOCA"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
	}

	@Test
	@DisplayName("Administrador publica, edita e remove uma novidade")
	void adminManagesNews() throws Exception {
		String admin = adminToken();
		long id = id(mvc.perform(post("/news").with(bearer(admin)).contentType(MediaType.APPLICATION_JSON)
				.content(news("Alinhamento com 20% de desconto")))
				.andExpect(status().isCreated())
				.andReturn());

		mvc.perform(put("/news/{id}", id).with(bearer(admin)).contentType(MediaType.APPLICATION_JSON)
				.content(news("Alinhamento com 25% de desconto")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Alinhamento com 25% de desconto"));

		mvc.perform(delete("/news/{id}", id).with(bearer(admin))).andExpect(status().isNoContent());
		mvc.perform(get("/news/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Cliente não publica novidade (403)")
	void customerCannotPublish() throws Exception {
		mvc.perform(post("/news").with(bearer(customerToken())).contentType(MediaType.APPLICATION_JSON)
				.content(news("Promoção falsa")))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Publicar sem token responde 401")
	void publishWithoutToken() throws Exception {
		mvc.perform(post("/news").contentType(MediaType.APPLICATION_JSON).content(news("Sem token")))
				.andExpect(status().isUnauthorized());
	}

	private static String news(String title) {
		return json("category", "OFFER", "title", title, "summary", "Válido em toda a rede até o fim do mês.", "body",
				"Agende pelo app e apresente o cupom na recepção.");
	}
}
