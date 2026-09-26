package com.fordretain.api.vehicle;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.fordretain.api.support.IntegrationTest;

@DisplayName("Veículos, laudo e revisão recomendada")
class VehicleIntegrationTest extends IntegrationTest {

	@Test
	@DisplayName("Cliente lista só os próprios veículos")
	void customerListsOwnVehicles() throws Exception {
		mvc.perform(get("/vehicles").with(bearer(customerToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].plate").value("BRA2E19"));
	}

	@Test
	@DisplayName("Cliente não enxerga o veículo de outro cliente (404)")
	void customerCannotSeeOthersVehicle() throws Exception {
		mvc.perform(get("/vehicles/{id}", TERRITORY_ID).with(bearer(customerToken())))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	@DisplayName("Administrador enxerga qualquer veículo")
	void adminSeesAnyVehicle() throws Exception {
		mvc.perform(get("/vehicles/{id}", TERRITORY_ID).with(bearer(adminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.plate").value("FRD1A24"));
	}

	@Test
	@DisplayName("Cadastro de veículo responde 201 com Location e componentes aguardando leitura")
	void createVehicle() throws Exception {
		Customer customer = newCustomer();

		mvc.perform(post("/vehicles").with(bearer(customer.token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("plate", "zzz-1d23", "model", "Maverick Lariat", "modelYear", 2025,
						"color", "Cinza Carbono", "mileageKm", 800)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", matchesPattern(".*/vehicles/\\d+$")))
				.andExpect(jsonPath("$.plate").value("ZZZ1D23"))
				.andExpect(jsonPath("$.ownerId").value(customer.id()));

		mvc.perform(get("/vehicles").with(bearer(customer.token())))
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	@DisplayName("Veículo recém-cadastrado tem os 10 componentes em dia")
	void newVehicleHealth() throws Exception {
		Customer customer = newCustomer();
		long vehicleId = newVehicle(customer.token());

		mvc.perform(get("/vehicles/{id}/health", vehicleId).with(bearer(customer.token())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.overall.score").value(100))
				.andExpect(jsonPath("$.counts.ok").value(10))
				.andExpect(jsonPath("$.systems[*].status", everyItem(is("OK"))));
	}

	@Test
	@DisplayName("Placa já cadastrada responde 409")
	void duplicatePlate() throws Exception {
		mvc.perform(post("/vehicles").with(bearer(newCustomer().token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("plate", "BRA-2E19", "model", "Ranger", "modelYear", 2023, "color", "Preto", "mileageKm", 1)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("PLATE_ALREADY_REGISTERED"));
	}

	@Test
	@DisplayName("Placa fora do padrão responde 400")
	void invalidPlate() throws Exception {
		mvc.perform(post("/vehicles").with(bearer(newCustomer().token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("plate", "12-ABCD", "model", "Ranger", "modelYear", 2023, "color", "Preto", "mileageKm", 1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("plate"));
	}

	@Test
	@DisplayName("Atualização do veículo responde 200 com os novos dados")
	void updateVehicle() throws Exception {
		Customer customer = newCustomer();
		long vehicleId = newVehicle(customer.token());

		mvc.perform(put("/vehicles/{id}", vehicleId).with(bearer(customer.token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("model", "Ranger XLS 2.2 AT", "modelYear", 2024, "color", "Azul Belize", "mileageKm", 13_000)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.model").value("Ranger XLS 2.2 AT"))
				.andExpect(jsonPath("$.mileageKm").value(13_000));
	}

	@Test
	@DisplayName("Quilometragem menor que a atual responde 422")
	void mileageCannotDecrease() throws Exception {
		Customer customer = newCustomer();
		long vehicleId = newVehicle(customer.token());

		mvc.perform(put("/vehicles/{id}", vehicleId).with(bearer(customer.token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("model", "Ranger", "modelYear", 2024, "color", "Azul", "mileageKm", 100)))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.code").value("MILEAGE_DECREASED"));
	}

	@Test
	@DisplayName("Remoção responde 204 e o veículo deixa de existir")
	void deleteVehicle() throws Exception {
		Customer customer = newCustomer();
		long vehicleId = newVehicle(customer.token());

		mvc.perform(delete("/vehicles/{id}", vehicleId).with(bearer(customer.token())))
				.andExpect(status().isNoContent());
		mvc.perform(get("/vehicles/{id}", vehicleId).with(bearer(customer.token())))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Veículo com revisão agendada não pode ser removido (409)")
	void cannotDeleteVehicleWithActiveBooking() throws Exception {
		Customer customer = newCustomer();
		long vehicleId = newVehicle(customer.token());
		book(customer.token(), vehicleId, VILA_PRUDENTE_ID);

		mvc.perform(delete("/vehicles/{id}", vehicleId).with(bearer(customer.token())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("VEHICLE_HAS_ACTIVE_BOOKINGS"));
	}

	@Test
	@DisplayName("Laudo da Ranger: 67 geral, óleo e pastilha urgentes, sistemas com a variação da semana")
	void rangerHealthReport() throws Exception {
		mvc.perform(get("/vehicles/{id}/health", RANGER_ID).with(bearer(customerToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.overall.score").value(67))
				.andExpect(jsonPath("$.overall.delta").value(-2))
				.andExpect(jsonPath("$.overall.status").value("URGENT"))
				.andExpect(jsonPath("$.counts.urgent").value(2))
				.andExpect(jsonPath("$.systems[0].name").value("Motor"))
				.andExpect(jsonPath("$.systems[0].score").value(69))
				.andExpect(jsonPath("$.systems[0].components[0].type").value("OIL"))
				.andExpect(jsonPath("$.systems[1].score").value(60))
				.andExpect(jsonPath("$.systems[3].status").value("OK"));
	}

	@Test
	@DisplayName("Revisão recomendada da Ranger soma R$ 1.595 em peças e mão de obra")
	void rangerServiceQuote() throws Exception {
		mvc.perform(get("/vehicles/{id}/service-quote", RANGER_ID).with(bearer(customerToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.serviceRecommended").value(true))
				.andExpect(jsonPath("$.items", hasSize(5)))
				.andExpect(jsonPath("$.totalCents").value(159_500))
				.andExpect(jsonPath("$.durationMinutes").value(90));
	}

	@Test
	@DisplayName("Administrador registra a leitura de um componente e o laudo reflete a nova nota")
	void adminRecordsReading() throws Exception {
		Customer customer = newCustomer();
		long vehicleId = newVehicle(customer.token());

		mvc.perform(put("/vehicles/{id}/components/{type}", vehicleId, "OIL").with(bearer(adminToken()))
				.contentType(MediaType.APPLICATION_JSON).content(json("health", 30, "detail", "Vencido há 900 km")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.health").value(30))
				.andExpect(jsonPath("$.status").value("URGENT"));

		mvc.perform(get("/vehicles/{id}/service-quote", vehicleId).with(bearer(customer.token())))
				.andExpect(jsonPath("$.items[0].component").value("OIL"));
	}

	@Test
	@DisplayName("Cliente não registra leitura de componente (403)")
	void customerCannotRecordReading() throws Exception {
		mvc.perform(put("/vehicles/{id}/components/{type}", RANGER_ID, "OIL").with(bearer(customerToken()))
				.contentType(MediaType.APPLICATION_JSON).content(json("health", 100)))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Leitura fora da faixa de 0 a 100 responde 400")
	void readingOutOfRange() throws Exception {
		mvc.perform(put("/vehicles/{id}/components/{type}", RANGER_ID, "OIL").with(bearer(adminToken()))
				.contentType(MediaType.APPLICATION_JSON).content(json("health", 140)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("health"));
	}
}
