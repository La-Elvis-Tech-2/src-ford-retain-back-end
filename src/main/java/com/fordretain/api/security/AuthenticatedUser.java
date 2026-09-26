package com.fordretain.api.security;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;

import com.fordretain.api.model.enums.Role;
import com.fordretain.api.service.TokenService;

public record AuthenticatedUser(Long id, Role role, Long dealerId) {

	public static AuthenticatedUser from(Jwt jwt) {
		List<String> roles = jwt.getClaimAsStringList(TokenService.ROLES_CLAIM);
		Object dealer = jwt.getClaims().get(TokenService.DEALER_CLAIM);
		Long dealerId = dealer instanceof Number number ? number.longValue() : null;
		return new AuthenticatedUser(Long.valueOf(jwt.getSubject()), Role.valueOf(roles.get(0)), dealerId);
	}

	public boolean is(Role expected) {
		return role == expected;
	}
}
