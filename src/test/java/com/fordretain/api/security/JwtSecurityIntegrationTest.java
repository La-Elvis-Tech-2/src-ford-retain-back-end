package com.fordretain.api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.fordretain.api.support.IntegrationTest;

@DisplayName("JWT: proteção dos recursos pelo token")
class JwtSecurityIntegrationTest extends IntegrationTest {

	@Autowired
	private JwtEncoder encoder;

	@Test
	@DisplayName("Recurso protegido sem token responde 401 com WWW-Authenticate")
	void missingToken() throws Exception {
		mvc.perform(get("/vehicles"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", "Bearer"))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
	}

	@Test
	@DisplayName("Token válido dá acesso ao recurso protegido")
	void validToken() throws Exception {
		mvc.perform(get("/vehicles").with(bearer(customerToken())))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Token expirado responde 401")
	void expiredToken() throws Exception {
		Instant issued = Instant.now().minus(Duration.ofHours(3));
		String token = sign(encoder, claims("ford-retain-api", List.of("CUSTOMER"), issued, issued.plus(Duration.ofHours(1))));

		mvc.perform(get("/vehicles").with(bearer(token)))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", "Bearer error=\"invalid_token\""))
				.andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
	}

	@Test
	@DisplayName("Token com a assinatura adulterada responde 401")
	void tamperedToken() throws Exception {
		String token = customerToken();
		String tampered = token.substring(0, token.length() - 4) + (token.endsWith("AAAA") ? "BBBB" : "AAAA");

		mvc.perform(get("/vehicles").with(bearer(tampered)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
	}

	@Test
	@DisplayName("Token assinado com outra chave responde 401")
	void tokenSignedWithAnotherKey() throws Exception {
		JwtEncoder foreign = NimbusJwtEncoder.withSecretKey(new SecretKeySpec(
				"outra-chave-que-nao-e-a-da-api-ford-retain".getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
				.algorithm(MacAlgorithm.HS256).build();
		String token = sign(foreign, claims("ford-retain-api", List.of("ADMIN"), Instant.now(), Instant.now().plus(Duration.ofHours(1))));

		mvc.perform(get("/users").with(bearer(token)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Token de outro emissor responde 401")
	void tokenFromAnotherIssuer() throws Exception {
		String token = sign(encoder, claims("outro-emissor", List.of("CUSTOMER"), Instant.now(), Instant.now().plus(Duration.ofHours(1))));

		mvc.perform(get("/vehicles").with(bearer(token)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Token sem perfil (roles) responde 401")
	void tokenWithoutRoles() throws Exception {
		String token = sign(encoder, claims("ford-retain-api", List.of(), Instant.now(), Instant.now().plus(Duration.ofHours(1))));

		mvc.perform(get("/vehicles").with(bearer(token)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Endpoints públicos respondem sem token")
	void publicEndpoints() throws Exception {
		mvc.perform(get("/dealers")).andExpect(status().isOk());
		mvc.perform(get("/dealers/{id}/slots", TATUAPE_ID)).andExpect(status().isOk());
		mvc.perform(get("/news")).andExpect(status().isOk());
		mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
		mvc.perform(get("/actuator/health")).andExpect(status().isOk());
	}

	@Test
	@DisplayName("Perfil sem permissão responde 403: cliente na lista de usuários")
	void customerForbiddenOnAdminResource() throws Exception {
		mvc.perform(get("/users").with(bearer(customerToken())))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
	}

	@Test
	@DisplayName("Perfil sem permissão responde 403: atendente nos veículos dos clientes")
	void dealerForbiddenOnVehicles() throws Exception {
		mvc.perform(get("/vehicles").with(bearer(dealerToken())))
				.andExpect(status().isForbidden());
	}

	private static JwtClaimsSet claims(String issuer, List<String> roles, Instant issuedAt, Instant expiresAt) {
		return JwtClaimsSet.builder()
				.issuer(issuer)
				.subject("4")
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.claim("roles", roles)
				.build();
	}

	private static String sign(JwtEncoder encoder, JwtClaimsSet claims) {
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
		return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
