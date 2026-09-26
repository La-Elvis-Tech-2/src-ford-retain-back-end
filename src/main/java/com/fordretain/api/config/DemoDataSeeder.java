package com.fordretain.api.config;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fordretain.api.model.Booking;
import com.fordretain.api.model.Dealer;
import com.fordretain.api.model.News;
import com.fordretain.api.model.ServiceSlot;
import com.fordretain.api.model.User;
import com.fordretain.api.model.Vehicle;
import com.fordretain.api.model.enums.BookingStatus;
import com.fordretain.api.model.enums.ComponentType;
import com.fordretain.api.model.enums.NewsCategory;
import com.fordretain.api.model.enums.Role;
import com.fordretain.api.repository.BookingRepository;
import com.fordretain.api.repository.DealerRepository;
import com.fordretain.api.repository.NewsRepository;
import com.fordretain.api.repository.ServiceSlotRepository;
import com.fordretain.api.repository.UserRepository;
import com.fordretain.api.repository.VehicleRepository;

/**
 * Dados de demonstração, carregados quando o banco está vazio.
 *
 * Reproduz o cenário do app mobile: a Ranger com óleo e pastilha urgentes, as
 * três concessionárias no caminho do cliente com a agenda das próximas
 * semanas e as novidades da rede. Há uma conta de cada perfil e um segundo
 * cliente, para demonstrar que um cliente não enxerga os dados do outro.
 *
 * Desligue com {@code DEMO_DATA=false}.
 */
@Component
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
	private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
	private static final int AGENDA_DAYS = 21;

	private final UserRepository users;
	private final DealerRepository dealers;
	private final ServiceSlotRepository slots;
	private final VehicleRepository vehicles;
	private final BookingRepository bookings;
	private final NewsRepository news;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	public DemoDataSeeder(UserRepository users, DealerRepository dealers, ServiceSlotRepository slots,
			VehicleRepository vehicles, BookingRepository bookings, NewsRepository news, PasswordEncoder passwordEncoder,
			Clock clock) {
		this.users = users;
		this.dealers = dealers;
		this.slots = slots;
		this.vehicles = vehicles;
		this.bookings = bookings;
		this.news = news;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (users.count() > 0) {
			return;
		}
		Instant now = clock.instant();

		Dealer tatuape = dealer("Ford Tatuapé", "R. Serra de Bragança, 1.302", "Tatuapé, São Paulo", "4.8", 2107, now);
		Dealer aricanduva = dealer("Ford Aricanduva", "Av. Aricanduva, 5.555", "Vila Matilde, São Paulo", "4.7", 1284, now);
		Dealer vilaPrudente = dealer("Ford Vila Prudente", "Av. Prof. Luiz Ignácio Anhaia Mello, 2.140",
				"Vila Prudente, São Paulo", "4.5", 862, now);

		agenda(tatuape, Map.of(DayOfWeek.MONDAY, List.of("09:00"), DayOfWeek.WEDNESDAY, List.of("14:00"),
				DayOfWeek.SATURDAY, List.of("08:00", "10:30")));
		agenda(aricanduva, Map.of(DayOfWeek.TUESDAY, List.of("10:00"), DayOfWeek.THURSDAY, List.of("09:00", "14:30"),
				DayOfWeek.FRIDAY, List.of("08:30")));
		agenda(vilaPrudente, Map.of(DayOfWeek.MONDAY, List.of("13:00"), DayOfWeek.FRIDAY, List.of("08:30", "16:00")));

		user("Administrador da Rede", "admin@fordretain.com", "Admin@123", Role.ADMIN, null, now);
		user("Atendimento Tatuapé", "tatuape@fordretain.com", "Concessionaria@123", Role.DEALER, tatuape, now);
		user("Atendimento Aricanduva", "aricanduva@fordretain.com", "Concessionaria@123", Role.DEALER, aricanduva, now);
		User vitor = user("Vitor Alves", "cliente@fordretain.com", "Cliente@123", Role.CUSTOMER, null, now);
		User ana = user("Ana Souza", "ana.souza@fordretain.com", "Cliente@123", Role.CUSTOMER, null, now);

		Vehicle ranger = ranger(vitor, now);
		territory(ana, now);
		lastService(vitor, ranger, tatuape, now);
		news(now);

		log.info("Dados de demonstração carregados: {} usuários, {} concessionárias, {} horários.", users.count(),
				dealers.count(), slots.count());
	}

	private Dealer dealer(String name, String address, String district, String rating, int reviews, Instant now) {
		return dealers.save(new Dealer(name, address, district, new BigDecimal(rating), reviews, now));
	}

	/** Os horários da semana viram datas reais a partir de amanhã, no fuso de São Paulo. */
	private void agenda(Dealer dealer, Map<DayOfWeek, List<String>> openings) {
		LocalDate today = LocalDate.now(clock.withZone(SAO_PAULO));
		for (int offset = 1; offset <= AGENDA_DAYS; offset++) {
			LocalDate day = today.plusDays(offset);
			for (String time : openings.getOrDefault(day.getDayOfWeek(), List.of())) {
				Instant startsAt = day.atTime(LocalTime.parse(time)).atZone(SAO_PAULO).toInstant();
				slots.save(new ServiceSlot(dealer, startsAt));
			}
		}
	}

	private User user(String name, String email, String password, Role role, Dealer dealer, Instant now) {
		return users.save(new User(name, email, passwordEncoder.encode(password), role, dealer, now));
	}

	private Vehicle ranger(User owner, Instant now) {
		Vehicle ranger = new Vehicle(owner, "BRA2E19", "Ranger Limited 2.0", 2023, "Preto Sublime", 78_420, now);
		ranger.addReading(ComponentType.OIL, 18, 26, "Vencido há 1.850 km · última troca aos 66.570 km", now);
		ranger.addReading(ComponentType.BRAKE_PAD, 31, 35, "Dianteira com 2,1 mm · ~1.100 km restantes", now);
		ranger.addReading(ComponentType.BATTERY, 55, 58, "12,1 V na partida a frio · 3 anos e 2 meses de uso", now);
		ranger.addReading(ComponentType.AIR_FILTER, 58, 61, "Saturação estimada em 78% · ~3.400 km restantes", now);
		ranger.addReading(ComponentType.CABIN_FILTER, 64, 66, "Em uso há 14 meses · ~2.900 km restantes", now);
		ranger.addReading(ComponentType.TIRES, 84, 82, "Sulco médio de 4,8 mm · desgaste uniforme nos quatro", now);
		ranger.addReading(ComponentType.SPARK_PLUGS, 86, 87, "Trocadas aos 71.800 km · próxima aos 91.800 km", now);
		ranger.addReading(ComponentType.BRAKE_FLUID, 88, 88, "Trocado em 11/2025 · próxima troca em 11/2027", now);
		ranger.addReading(ComponentType.TIMING_BELT, 92, 92, "Trocada aos 61.300 km · próxima aos 121.300 km", now);
		ranger.addReading(ComponentType.COOLANT, 95, 95, "Nível normal · temperatura estável em 91 °C", now);
		return vehicles.save(ranger);
	}

	private void territory(User owner, Instant now) {
		Vehicle territory = new Vehicle(owner, "FRD1A24", "Territory Titanium", 2024, "Branco Ártico", 18_300, now);
		territory.addReading(ComponentType.OIL, 82, 84, "Troca prevista em ~3.700 km", now);
		territory.addReading(ComponentType.BRAKE_PAD, 76, 77, "Dianteira com 6,4 mm", now);
		territory.addReading(ComponentType.BATTERY, 91, 91, "12,6 V na partida a frio", now);
		territory.addReading(ComponentType.AIR_FILTER, 88, 88, "Saturação estimada em 22%", now);
		territory.addReading(ComponentType.CABIN_FILTER, 70, 71, "Em uso há 11 meses · ~900 km restantes", now);
		territory.addReading(ComponentType.TIRES, 79, 80, "Sulco médio de 5,9 mm", now);
		territory.addReading(ComponentType.SPARK_PLUGS, 95, 95, "Originais de fábrica", now);
		territory.addReading(ComponentType.BRAKE_FLUID, 90, 90, "Troca prevista em 03/2027", now);
		territory.addReading(ComponentType.TIMING_BELT, 97, 97, "Original de fábrica", now);
		territory.addReading(ComponentType.COOLANT, 96, 96, "Nível normal", now);
		vehicles.save(territory);
	}

	/** A revisão já feita na rede: o histórico que o laudo mostra ao cliente. */
	private void lastService(User customer, Vehicle vehicle, Dealer dealer, Instant now) {
		ServiceSlot past = new ServiceSlot(dealer, now.minus(Duration.ofDays(330)));
		past.reserve();
		slots.save(past);

		Booking done = new Booking(customer, vehicle, past, BookingStatus.REQUESTED, 890_00, past.getStartsAt());
		done.moveTo(BookingStatus.CONFIRMED, past.getStartsAt());
		done.moveTo(BookingStatus.COMPLETED, past.getStartsAt().plus(Duration.ofHours(2)));
		bookings.save(done);
	}

	private void news(Instant now) {
		news.save(new News(NewsCategory.SAFETY, "Nenhum recall pendente no seu chassi",
				"Conferimos sua Ranger contra todas as campanhas abertas da Ford no Brasil.",
				"Toda semana o chassi da sua Ranger é cruzado com as campanhas de recall abertas pela Ford no Brasil. "
						+ "Hoje não há nenhuma pendência.",
				now));
		news.save(new News(NewsCategory.OFFER, "Teste de bateria grátis até 31 de outubro",
				"Com a virada do tempo, a partida a frio cobra mais da bateria. O teste leva 10 minutos.",
				"O teste de carga é gratuito em toda a rede até 31 de outubro. A troca só é indicada se a bateria reprovar.",
				now.minus(Duration.ofDays(2))));
		news.save(new News(NewsCategory.LAUNCH, "Ranger Raptor 2027 chega às concessionárias",
				"Motor V6 biturbo de 397 cv e suspensão Fox Live Valve. Test-drive já pode ser agendado.",
				"A Ranger Raptor 2027 chega com motor V6 3.0 biturbo de 397 cv, suspensão Fox Live Valve e seis modos de "
						+ "condução. Clientes com histórico Ford completo têm avaliação do usado com prioridade na troca.",
				now.minus(Duration.ofDays(5))));
	}
}
