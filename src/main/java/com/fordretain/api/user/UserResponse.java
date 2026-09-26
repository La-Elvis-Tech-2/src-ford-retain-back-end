package com.fordretain.api.user;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Conta de usuário, sem dados de credencial.")
public record UserResponse(
		Long id,
		String name,
		String email,
		Role role,
		@Schema(description = "Concessionária de quem tem o perfil DEALER") Long dealerId,
		Instant createdAt) {

	public static UserResponse from(User user) {
		Long dealerId = user.getDealer() != null ? user.getDealer().getId() : null;
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), dealerId,
				user.getCreatedAt());
	}
}
