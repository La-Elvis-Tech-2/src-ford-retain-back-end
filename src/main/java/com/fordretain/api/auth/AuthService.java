package com.fordretain.api.auth;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fordretain.api.common.error.InvalidCredentialsException;
import com.fordretain.api.security.TokenService;
import com.fordretain.api.security.TokenService.IssuedToken;
import com.fordretain.api.user.User;
import com.fordretain.api.user.UserRepository;
import com.fordretain.api.user.UserResponse;
import com.fordretain.api.user.UserService;

@Service
public class AuthService {

	/**
	 * Hash de uma senha qualquer, usado quando o e-mail não existe: o BCrypt roda
	 * do mesmo jeito, e o tempo de resposta não revela quais e-mails têm conta.
	 */
	private final String dummyHash;

	private final UserRepository users;
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;

	public AuthService(UserRepository users, UserService userService, PasswordEncoder passwordEncoder,
			TokenService tokenService) {
		this.users = users;
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
		this.dummyHash = passwordEncoder.encode("ford-retain-dummy-password");
	}

	@Transactional
	public User register(RegisterRequest request) {
		return userService.registerCustomer(request.name(), request.email(), request.password());
	}

	@Transactional(readOnly = true)
	public TokenResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		User user = users.findByEmail(email).orElse(null);
		String hash = user != null ? user.getPasswordHash() : dummyHash;

		if (!passwordEncoder.matches(request.password(), hash) || user == null) {
			throw new InvalidCredentialsException();
		}

		IssuedToken token = tokenService.issue(user);
		return new TokenResponse(token.value(), "Bearer", token.expiresInSeconds(), token.expiresAt(),
				UserResponse.from(user));
	}
}
