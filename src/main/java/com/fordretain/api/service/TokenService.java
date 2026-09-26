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

	public record IssuedToken(String value, Instant expiresAt, long expiresInSeconds) {
	}
}
