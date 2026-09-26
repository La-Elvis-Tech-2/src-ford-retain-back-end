package com.fordretain.api.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fordretain.api.repository.ServiceSlotRepository;
import com.fordretain.api.support.IntegrationTest;

@DisplayName("Agendamentos de revisão")
class BookingIntegrationTest extends IntegrationTest {

	@Autowired
	private ServiceSlotRepository slots;

	@Test
	@DisplayName("Cliente agenda a revisão: 201 com Location, status REQUESTED e horário reservado")
	void customerBooksService() throws Exception {
		Customer customer = newCustomer();
		long vehicleId = newVehicle(customer.token());
		long slotId = firstAvailableSlot(TATUAPE_ID);

		mvc.perform(post("/bookings").with(bearer(customer.token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("vehicleId", vehicleId, "slotId", slotId)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", matchesPattern(".*/bookings/\\d+$")))
				.andExpect(jsonPath("$.status").value("REQUESTED"))
				.andExpect(jsonPath("$.dealer.name").value("Ford Tatuapé"))
				.andExpect(jsonPath("$.vehicle.id").value(vehicleId));

		mvc.perform(get("/dealers/{id}/slots/{slotId}", TATUAPE_ID, slotId))
				.andExpect(jsonPath("$.available").value(false));
		mvc.perform(get("/dealers/{id}/slots", TATUAPE_ID))
				.andExpect(jsonPath("$[*].id", not(hasItem((int) slotId))));
	}

	@Test
	@DisplayName("Horário já reservado responde 409")
	void slotAlreadyTaken() throws Exception {
		Customer first = newCustomer();
		long slotId = firstAvailableSlot(ARICANDUVA_ID);
		mvc.perform(post("/bookings").with(bearer(first.token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("vehicleId", newVehicle(first.token()), "slotId", slotId)))
				.andExpect(status().isCreated());

		Customer second = newCustomer();
		mvc.perform(post("/bookings").with(bearer(second.token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("vehicleId", newVehicle(second.token()), "slotId", slotId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("SLOT_UNAVAILABLE"));
	}

	@Test
	@DisplayName("Veículo de outro cliente não pode ser agendado (404)")
	void cannotBookOthersVehicle() throws Exception {
		mvc.perform(post("/bookings").with(bearer(newCustomer().token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("vehicleId", RANGER_ID, "slotId", firstAvailableSlot(TATUAPE_ID))))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Horário no passado responde 422")
	void pastSlot() throws Exception {
		Customer customer = newCustomer();
		long pastSlot = slots.findAll().stream().filter(slot -> !slot.isAvailable()
				&& slot.getStartsAt().isBefore(Instant.now())).findFirst().orElseThrow().getId();

		mvc.perform(post("/bookings").with(bearer(customer.token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("vehicleId", newVehicle(customer.token()), "slotId", pastSlot)))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.code").value("SLOT_IN_PAST"));
	}

	@Test
	@DisplayName("Veículo com revisão em aberto não agenda outra (409)")
	void vehicleAlreadyBooked() throws Exception {
		Customer customer = newCustomer();
		long vehicleId = newVehicle(customer.token());
		book(customer.token(), vehicleId, VILA_PRUDENTE_ID);

		mvc.perform(post("/bookings").with(bearer(customer.token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("vehicleId", vehicleId, "slotId", firstAvailableSlot(VILA_PRUDENTE_ID))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("VEHICLE_ALREADY_BOOKED"));
	}

	@Test
	@DisplayName("Atendente não cria agendamento em nome de cliente (403)")
	void dealerCannotCreateBooking() throws Exception {
		mvc.perform(post("/bookings").with(bearer(dealerToken())).contentType(MediaType.APPLICATION_JSON)
				.content(json("vehicleId", RANGER_ID, "slotId", firstAvailableSlot(TATUAPE_ID))))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Cada perfil vê o seu recorte: cliente os próprios, atendente os da concessionária")
	void listingIsScopedByRole() throws Exception {
		Customer customer = newCustomer();
		long bookingId = book(customer.token(), newVehicle(customer.token()), TATUAPE_ID);

		mvc.perform(get("/bookings").with(bearer(customer.token())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].customerId", everyItem(is((int) customer.id()))));
		mvc.perform(get("/bookings").with(bearer(dealerToken())))
				.andExpect(jsonPath("$[*].dealer.id", everyItem(is((int) TATUAPE_ID))))
				.andExpect(jsonPath("$[*].id", hasItem((int) bookingId)));
		mvc.perform(get("/bookings").with(bearer(login(OTHER_DEALER, DEALER_PASSWORD))))
				.andExpect(jsonPath("$[*].id", not(hasItem((int) bookingId))));
	}

	@Test
	@DisplayName("Atendente de outra concessionária não enxerga o agendamento (404)")
	void otherDealerCannotSeeBooking() throws Exception {
		Customer customer = newCustomer();
		long bookingId = book(customer.token(), newVehicle(customer.token()), TATUAPE_ID);

		mvc.perform(get("/bookings/{id}", bookingId).with(bearer(login(OTHER_DEALER, DEALER_PASSWORD))))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Atendente confirma e depois conclui a revisão")
	void dealerConfirmsAndCompletes() throws Exception {
		Customer customer = newCustomer();
		long bookingId = book(customer.token(), newVehicle(customer.token()), TATUAPE_ID);
		String dealer = dealerToken();

		mvc.perform(patch("/bookings/{id}", bookingId).with(bearer(dealer)).contentType(MediaType.APPLICATION_JSON)
				.content(json("status", "CONFIRMED")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMED"));
		mvc.perform(patch("/bookings/{id}", bookingId).with(bearer(dealer)).contentType(MediaType.APPLICATION_JSON)
				.content(json("status", "COMPLETED")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));
	}

	@Test
	@DisplayName("Cliente não confirma o próprio agendamento (403)")
	void customerCannotConfirm() throws Exception {
		Customer customer = newCustomer();
		long bookingId = book(customer.token(), newVehicle(customer.token()), ARICANDUVA_ID);

		mvc.perform(patch("/bookings/{id}", bookingId).with(bearer(customer.token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("status", "CONFIRMED")))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Cliente cancela e o horário volta a ficar livre")
	void customerCancelsAndSlotIsReleased() throws Exception {
		Customer customer = newCustomer();
		long slotId = firstAvailableSlot(VILA_PRUDENTE_ID);
		long bookingId = id(mvc.perform(post("/bookings").with(bearer(customer.token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("vehicleId", newVehicle(customer.token()), "slotId", slotId)))
				.andExpect(status().isCreated()).andReturn());

		mvc.perform(patch("/bookings/{id}", bookingId).with(bearer(customer.token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("status", "CANCELLED")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));
		mvc.perform(get("/dealers/{id}/slots/{slotId}", VILA_PRUDENTE_ID, slotId))
				.andExpect(jsonPath("$.available").value(true));
	}

	@Test
	@DisplayName("Agendamento cancelado não pode ser confirmado (409)")
	void invalidTransition() throws Exception {
		Customer customer = newCustomer();
		long bookingId = book(customer.token(), newVehicle(customer.token()), TATUAPE_ID);
		mvc.perform(patch("/bookings/{id}", bookingId).with(bearer(customer.token())).contentType(MediaType.APPLICATION_JSON)
				.content(json("status", "CANCELLED")))
				.andExpect(status().isOk());

		mvc.perform(patch("/bookings/{id}", bookingId).with(bearer(dealerToken())).contentType(MediaType.APPLICATION_JSON)
				.content(json("status", "CONFIRMED")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
	}

	@Test
	@DisplayName("Status inexistente no corpo responde 400")
	void unknownStatus() throws Exception {
		mvc.perform(patch("/bookings/{id}", 1).with(bearer(adminToken())).contentType(MediaType.APPLICATION_JSON)
				.content(json("status", "ADIADO")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
	}
}
