package com.fordretain.api.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.security.jwt")
public record JwtProperties(String secret, String issuer, Duration expiration) {

	private static final int MIN_SECRET_BYTES = 32;

	public JwtProperties {
		if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
			throw new IllegalStateException("app.security.jwt.secret precisa ter pelo menos 32 bytes para o HS256.");
		}
		if (expiration == null || expiration.isNegative() || expiration.isZero()) {
			throw new IllegalStateException("app.security.jwt.expiration precisa ser positivo.");
		}
	}
}
