package com.example.r2dbc;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;


@RequiredArgsConstructor
@EnableTransactionManagement
@SpringBootApplication
public class R2dbcApplication {

	public static void main(String[] args) {
		SpringApplication.run(R2dbcApplication.class, args);
	}

	@Bean
	ConnectionFactory connectionFactory(@Value("${spring.r2dbc.url}") String url) {
		return ConnectionFactories.get(url);
	}

	@Bean
	ReactiveTransactionManager transactionManager(ConnectionFactory cf) {
		return new R2dbcTransactionManager(cf);
	}

	@Bean
	TransactionalOperator transactionalOperator(ReactiveTransactionManager txm) {
		return TransactionalOperator.create(txm);
	}
}


@Component
@RequiredArgsConstructor
@Log4j2
class R2dbcDemo {

	private final ReservationService reservationService;

	@EventListener(ApplicationReadyEvent.class)
	public void serviceDemo() throws Exception {

		/*this.reservationService
			.save(new Reservation(null, "Jane"))
			.thenMany(this.reservationService.findAll())
			.subscribe(log::info);
		*/


		this.reservationService.saveNames("Albert", "Bob", "carter")
			.thenMany(this.reservationService.findAll())
			.subscribe(log::info);


	}
}


interface ReservationRepository extends ReactiveCrudRepository<Reservation, Integer> {
}

@Service
@RequiredArgsConstructor
class SpringDataReservationSerfvice implements ReservationService {

	private final ReservationRepository repository;

	@Override
	@Transactional
	public Flux<Reservation> saveNames(String... names) {
		return Flux
			.just(names)
			.map(r -> new Reservation(null, r))
			.flatMap(this::save)
			.doOnNext(r -> validateName(r.getName()));

	}

	private void validateName(String name) {
		Assert.isTrue(Character.isUpperCase(name.charAt(0)), "the character must be uppercase");
	}


	@Override
	public Mono<Reservation> save(Reservation r) {
		return this.repository.save(r);
	}

	@Override
	public Flux<Reservation> findAll() {
		return this.repository.findAll();
	}
}

//@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
class DatabaseClientReservationService implements ReservationService {

	private final DatabaseClient databaseClient;

	private final Function<Map<String, Object>, Reservation> mapper = map -> new Reservation((Integer) map.get("id"), (String) map.get("name"));

	@Override
	@Transactional
	public Flux<Reservation> saveNames(String... names) {
		return Flux
			.just(names)
			.map(r -> new Reservation(null, r))
			.flatMap(this::save)
			.doOnNext(r -> validateName(r.getName()));
	}

	private void validateName(String name) {
		Assert.isTrue(Character.isUpperCase(name.charAt(0)), "the character must be uppercase");
	}


	@Override
	public Mono<Reservation> save(Reservation r) {

		return this.databaseClient
			.insert()
			.into(Reservation.class)
			.table("reservation")
			.using(r)
			.fetch()
			.first()
			.map(mapper);
	}

	@Override
	public Flux<Reservation> findAll() {
		return this.databaseClient.select().from(Reservation.class).fetch().all();
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Reservation {

	@Id
	private Integer id;
	private String name;
}

interface ReservationService {

	Flux<Reservation> saveNames(String... names);

	Mono<Reservation> save(Reservation r);

	Flux<Reservation> findAll();

}