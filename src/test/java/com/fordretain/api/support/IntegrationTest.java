package com.fordretain.api.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.jayway.jsonpath.JsonPath;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTest {

	protected static final String CUSTOMER = "cliente@fordretain.com";
	protected static final String OTHER_CUSTOMER = "ana.souza@fordretain.com";
	protected static final String CUSTOMER_PASSWORD = "Cliente@123";
	protected static final String DEALER = "tatuape@fordretain.com";
	protected static final String OTHER_DEALER = "aricanduva@fordretain.com";
	protected static final String DEALER_PASSWORD = "Concessionaria@123";
	protected static final String ADMIN = "admin@fordretain.com";
	protected static final String ADMIN_PASSWORD = "Admin@123";

	protected static final long RANGER_ID = 1;
	protected static final long TERRITORY_ID = 2;
	protected static final long TATUAPE_ID = 1;
	protected static final long ARICANDUVA_ID = 2;
	protected static final long VILA_PRUDENTE_ID = 3;

	private static final JsonMapper MAPPER = JsonMapper.builder().build();
	private static final AtomicInteger SEQUENCE = new AtomicInteger();

	@Autowired
	protected MockMvc mvc;

	protected String login(String email, String password) throws Exception {
		MvcResult result = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json("email", email, "password", password)))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
	}

	protected String customerToken() throws Exception {
		return login(CUSTOMER, CUSTOMER_PASSWORD);
	}

	protected String dealerToken() throws Exception {
		return login(DEALER, DEALER_PASSWORD);
	}

	protected String adminToken() throws Exception {
		return login(ADMIN, ADMIN_PASSWORD);
	}

	protected Customer newCustomer() throws Exception {
		String email = "cliente-" + UUID.randomUUID() + "@teste.com";
		MvcResult result = mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(json("name", "Cliente de Teste", "email", email, "password", "Senha@2026")))
				.andExpect(status().isCreated())
				.andReturn();
		return new Customer(id(result), login(email, "Senha@2026"));
	}

	protected long newVehicle(String token) throws Exception {
		MvcResult result = mvc.perform(post("/vehicles").with(bearer(token)).contentType(MediaType.APPLICATION_JSON)
				.content(json("plate", uniquePlate(), "model", "Ranger XLS 2.2", "modelYear", 2024, "color", "Azul Belize",
						"mileageKm", 12_500)))
				.andExpect(status().isCreated())
				.andReturn();
		return id(result);
	}

	protected long firstAvailableSlot(long dealerId) throws Exception {
		MvcResult result = mvc.perform(get("/dealers/{id}/slots", dealerId)).andExpect(status().isOk()).andReturn();
		return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$[0].id")).longValue();
	}

	protected long book(String token, long vehicleId, long dealerId) throws Exception {
		MvcResult result = mvc.perform(post("/bookings").with(bearer(token)).contentType(MediaType.APPLICATION_JSON)
				.content(json("vehicleId", vehicleId, "slotId", firstAvailableSlot(dealerId))))
				.andExpect(status().isCreated())
				.andReturn();
		return id(result);
	}

	protected static RequestPostProcessor bearer(String token) {
		return request -> {
			request.addHeader("Authorization", "Bearer " + token);
			return request;
		};
	}

	protected static long id(MvcResult result) throws Exception {
		return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
	}

	protected static String json(Object... keysAndValues) {
		Map<String, Object> body = new LinkedHashMap<>();
		for (int i = 0; i < keysAndValues.length; i += 2) {
			body.put((String) keysAndValues[i], keysAndValues[i + 1]);
		}
		return MAPPER.writeValueAsString(body);
	}

	protected static String uniquePlate() {
		int n = SEQUENCE.incrementAndGet();
		return "TST" + (n % 10) + (char) ('A' + (n / 10) % 26) + String.format("%02d", (n / 260) % 100);
	}

	protected record Customer(long id, String token) {
	}
}
