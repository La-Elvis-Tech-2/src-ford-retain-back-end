package com.fordretain.api.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.fordretain.api.service.TokenService;

/**
 * Assinatura e validação do JWT com HMAC-SHA256.
 *
 * A validação confere, além da assinatura: expiração ({@code exp}) e início de
 * validade ({@code nbf}) contra o relógio da aplicação, o emissor ({@code iss})
 * e a presença do perfil ({@code roles}). Um token que falhe em qualquer uma
 * dessas regras é recusado com 401.
 */
@Configuration
public class JwtConfig {

	@Bean
	SecretKey jwtSigningKey(JwtProperties properties) {
		return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
		return NimbusJwtEncoder.withSecretKey(jwtSigningKey).algorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey jwtSigningKey, JwtProperties properties, Clock clock) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey).macAlgorithm(MacAlgorithm.HS256).build();

		JwtTimestampValidator timestamps = new JwtTimestampValidator();
		timestamps.setClock(clock);

		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				timestamps,
				new JwtIssuerValidator(properties.issuer()),
				new JwtClaimValidator<List<String>>(TokenService.ROLES_CLAIM, roles -> roles != null && !roles.isEmpty())));
		return decoder;
	}
}
