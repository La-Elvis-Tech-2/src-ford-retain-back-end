package com.fordretain.api.security;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Configuração do JWT")
class JwtPropertiesTest {

	@Test
	@DisplayName("Recusa segredo com menos de 32 bytes, fraco demais para o HS256")
	void rejectsShortSecret() {
		assertThatIllegalStateException()
				.isThrownBy(() -> new JwtProperties("curto", "ford-retain-api", Duration.ofHours(1)))
				.withMessageContaining("32 bytes");
	}

	@Test
	@DisplayName("Recusa validade zero ou negativa")
	void rejectsNonPositiveExpiration() {
		assertThatIllegalStateException()
				.isThrownBy(() -> new JwtProperties("x".repeat(32), "ford-retain-api", Duration.ZERO));
	}
}
