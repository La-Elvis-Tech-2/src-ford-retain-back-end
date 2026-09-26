package com.fordretain.api.user;

/**
 * Perfis de acesso.
 *
 * <ul>
 * <li>{@code CUSTOMER}: dono do veículo; vê a saúde dos próprios carros e agenda revisões.</li>
 * <li>{@code DEALER}: atendente de uma concessionária; abre horários e conduz os agendamentos dela.</li>
 * <li>{@code ADMIN}: operação da rede; mantém concessionárias, novidades, usuários e leituras.</li>
 * </ul>
 */
public enum Role {
	CUSTOMER,
	DEALER,
	ADMIN
}
