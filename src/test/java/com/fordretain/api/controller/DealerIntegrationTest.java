package com.fordretain.api.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.fordretain.api.support.IntegrationTest;

@DisplayName("Concessionárias e agenda da oficina")
class DealerIntegrationTest extends IntegrationTest {

	@Test
	@DisplayName("Lista de concessionárias é pública")
	void listIsPublic() throws Exception {
		mvc.perform(get("/dealers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
	}

	@Test
	@DisplayName("Concessionária inexistente responde 404")
	void unknownDealer() throws Exception {
		mvc.perform(get("/dealers/{id}", 9_999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	@DisplayName("Administrador cadastra concessionária: 201 com Location")
	void adminCreatesDealer() throws Exception {
		mvc.perform(post("/dealers").with(bearer(adminToken())).contentType(MediaType.APPLICATION_JSON)
				.content(dealer("Ford Mooca")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", matchesPattern(".*/dealers/\\d+$")))
				.andExpect(jsonPath("$.name").value("Ford Mooca"));
	}

	@Test
	@DisplayName("Cliente não cadastra concessionária (403)")
	void customerCannotCreateDealer() throws Exception {
		mvc.perform(post("/dealers").with(bearer(customerToken())).contentType(MediaType.APPLICATION_JSON)
				.content(dealer("Ford Pirata")))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Nota fora da faixa de 0 a 5 responde 400")
	void invalidRating() throws Exception {
		mvc.perform(post("/dealers").with(bearer(adminToken())).contentType(MediaType.APPLICATION_JSON)
				.content(json("name", "Ford X", "address", "Rua X", "district", "Centro", "rating", 7.5, "reviewCount", 1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("rating"));
	}

	@Test
	@DisplayName("Administrador atualiza e depois remove uma concessionária sem histórico")
	void adminUpdatesAndDeletes() throws Exception {
		String admin = adminToken();
		long id = id(mvc.perform(post("/dealers").with(bearer(admin)).contentType(MediaType.APPLICATION_JSON)
				.content(dealer("Ford Ipiranga"))).andReturn());

		mvc.perform(put("/dealers/{id}", id).with(bearer(admin)).contentType(MediaType.APPLICATION_JSON)
				.content(json("name", "Ford Ipiranga Centro", "address", "Av. Nazaré, 900", "district", "Ipiranga, São Paulo",
						"rating", 4.4, "reviewCount", 120)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Ford Ipiranga Centro"));

		mvc.perform(delete("/dealers/{id}", id).with(bearer(admin)))
				.andExpect(status().isNoContent());
		mvc.perform(get("/dealers/{id}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Concessionária com atendentes não pode ser removida (409)")
	void cannotDeleteDealerWithHistory() throws Exception {
		mvc.perform(delete("/dealers/{id}", TATUAPE_ID).with(bearer(adminToken())))
				.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("Agenda pública traz só horários futuros e livres")
	void publicSlots() throws Exception {
		mvc.perform(get("/dealers/{id}/slots", ARICANDUVA_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].available").value(true))
				.andExpect(jsonPath("$[0].dealerId").value(ARICANDUVA_ID));
	}

	@Test
	@DisplayName("Atendente abre horário na própria concessionária: 201")
	void dealerOpensSlot() throws Exception {
		mvc.perform(post("/dealers/{id}/slots", TATUAPE_ID).with(bearer(dealerToken())).contentType(MediaType.APPLICATION_JSON)
				.content(json("startsAt", futureSlot(40).toString())))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", matchesPattern(".*/dealers/1/slots/\\d+$")))
				.andExpect(jsonPath("$.available").value(true));
	}

	@Test
	@DisplayName("Atendente não abre horário em outra concessionária (403)")
	void dealerCannotOpenSlotElsewhere() throws Exception {
		mvc.perform(post("/dealers/{id}/slots", ARICANDUVA_ID).with(bearer(dealerToken())).contentType(MediaType.APPLICATION_JSON)
				.content(json("startsAt", futureSlot(41).toString())))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Horário no passado responde 422 e horário repetido responde 409")
	void slotRules() throws Exception {
		String dealer = dealerToken();
		mvc.perform(post("/dealers/{id}/slots", TATUAPE_ID).with(bearer(dealer)).contentType(MediaType.APPLICATION_JSON)
				.content(json("startsAt", Instant.now().minus(Duration.ofDays(1)).toString())))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.code").value("SLOT_IN_PAST"));

		String startsAt = futureSlot(42).toString();
		mvc.perform(post("/dealers/{id}/slots", TATUAPE_ID).with(bearer(dealer)).contentType(MediaType.APPLICATION_JSON)
				.content(json("startsAt", startsAt)))
				.andExpect(status().isCreated());
		mvc.perform(post("/dealers/{id}/slots", TATUAPE_ID).with(bearer(dealer)).contentType(MediaType.APPLICATION_JSON)
				.content(json("startsAt", startsAt)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("SLOT_ALREADY_EXISTS"));
	}

	private static String dealer(String name) {
		return json("name", name, "address", "R. da Mooca, 2.500", "district", "Mooca, São Paulo", "rating", 4.6,
				"reviewCount", 540);
	}

	private static Instant futureSlot(int days) {
		return Instant.now().plus(Duration.ofDays(days)).truncatedTo(ChronoUnit.HOURS);
	}
}
