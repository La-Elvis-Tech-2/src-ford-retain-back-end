package com.fordretain.api.booking;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Ciclo de vida do agendamento")
class BookingStatusTest {

	@ParameterizedTest(name = "{0} -> {1}: {2}")
	@CsvSource({
			"REQUESTED, CONFIRMED, true",
			"REQUESTED, CANCELLED, true",
			"REQUESTED, COMPLETED, false",
			"CONFIRMED, COMPLETED, true",
			"CONFIRMED, CANCELLED, true",
			"CONFIRMED, REQUESTED, false",
			"COMPLETED, CANCELLED, false",
			"CANCELLED, CONFIRMED, false" })
	@DisplayName("Permite só as transições do ciclo de vida")
	void transitions(BookingStatus from, BookingStatus to, boolean allowed) {
		assertThat(from.canMoveTo(to)).isEqualTo(allowed);
	}
}
