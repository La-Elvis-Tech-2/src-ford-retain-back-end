package com.fordretain.api.security;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;

import com.fordretain.api.user.Role;

/**
 * Quem está chamando a API, lido do token já validado.
 *
 * Os controllers recebem este tipo como parâmetro (ver
 * {@link AuthenticatedUserArgumentResolver}) e os services decidem a posse dos
 * recursos a partir dele: um cliente só enxerga os próprios veículos, uma
 * concessionária só os próprios agendamentos.
 */
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
