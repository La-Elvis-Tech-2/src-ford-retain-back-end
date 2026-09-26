package com.fordretain.api.user;

import java.time.Clock;
import java.util.List;
import java.util.Locale;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fordretain.api.common.error.BusinessRuleException;
import com.fordretain.api.common.error.ConflictException;
import com.fordretain.api.common.error.NotFoundException;
import com.fordretain.api.dealer.Dealer;
import com.fordretain.api.dealer.DealerRepository;
import com.fordretain.api.security.AuthenticatedUser;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository users;
	private final DealerRepository dealers;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	public UserService(UserRepository users, DealerRepository dealers, PasswordEncoder passwordEncoder, Clock clock) {
		this.users = users;
		this.dealers = dealers;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
	}

	/** Cadastro público: sempre cria um cliente. */
	@Transactional
	public User registerCustomer(String name, String email, String password) {
		return save(name, email, password, Role.CUSTOMER, null);
	}

	@Transactional
	public User create(CreateUserRequest request) {
		Dealer dealer = null;
		if (request.role() == Role.DEALER) {
			if (request.dealerId() == null) {
				throw new BusinessRuleException("DEALER_REQUIRED", "O perfil DEALER precisa de uma concessionária (dealerId).");
			}
			dealer = dealers.findById(request.dealerId())
					.orElseThrow(() -> new NotFoundException("Concessionária", request.dealerId()));
		} else if (request.dealerId() != null) {
			throw new BusinessRuleException("DEALER_NOT_ALLOWED", "Só o perfil DEALER pode ser vinculado a uma concessionária.");
		}
		return save(request.name(), request.email(), request.password(), request.role(), dealer);
	}

	public User get(Long id, AuthenticatedUser requester) {
		if (!requester.is(Role.ADMIN) && !requester.id().equals(id)) {
			throw new AccessDeniedException("Só o administrador consulta outras contas.");
		}
		return users.findById(id).orElseThrow(() -> new NotFoundException("Usuário", id));
	}

	public List<User> list() {
		return users.findAllByOrderByIdAsc();
	}

	private User save(String name, String email, String password, Role role, Dealer dealer) {
		String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
		if (users.existsByEmail(normalizedEmail)) {
			throw new ConflictException("EMAIL_ALREADY_REGISTERED", "Já existe uma conta com este e-mail.");
		}
		User user = new User(name.trim(), normalizedEmail, passwordEncoder.encode(password), role, dealer,
				clock.instant());
		return users.save(user);
	}
}
