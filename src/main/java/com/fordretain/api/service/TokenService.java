package com.fordretain.api.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.fordretain.api.model.User;
import com.fordretain.api.security.JwtProperties;

/**
 * Emite o token de acesso depois do login.
 *
 * O token carrega só o necessário para autorizar sem ir ao banco: o id do
 * usuário em {@code sub}, o perfil em {@code roles} e, para quem atende numa
 * concessionária, o {@code dealerId}. Nome e e-mail vão como informação de
 * exibição; nenhum dado sensível (senha, hash) entra no token.
 */
@Service
public class TokenService {

	public static final String ROLES_CLAIM = "roles";
	public static final String DEALER_CLAIM = "dealerId";

	private final JwtEncoder encoder;
	private final JwtProperties properties;
	private final Clock clock;

	public TokenService(JwtEncoder encoder, JwtProperties properties, Clock clock) {
		this.encoder = encoder;
		this.properties = properties;
		this.clock = clock;
	}

	public IssuedToken issue(User user) {
		Instant issuedAt = clock.instant();
		Instant expiresAt = issuedAt.plus(properties.expiration());

		JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
				.id(UUID.randomUUID().toString())
				.issuer(properties.issuer())
				.subject(user.getId().toString())
				.issuedAt(issuedAt)
				.notBefore(issuedAt)
				.expiresAt(expiresAt)
				.claim("name", user.getName())
				.claim("email", user.getEmail())
				.claim(ROLES_CLAIM, List.of(user.getRole().name()));
		if (user.getDealer() != null) {
			claims.claim(DEALER_CLAIM, user.getDealer().getId());
		}

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
		String value = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
		return new IssuedToken(value, expiresAt, properties.expiration().toSeconds());
	}

	/** O token assinado e quando ele deixa de valer. */
	public record IssuedToken(String value, Instant expiresAt, long expiresInSeconds) {
	}
}
