package com.example.data.r2dbc;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@SpringBootApplication(exclude = {
	MongoAutoConfiguration.class,
	MongoReactiveAutoConfiguration.class,
	MongoDataAutoConfiguration.class
})
public class R2dbcRepositoryApplication {

	public static void main(String args[]) {
		SpringApplication.run(R2dbcRepositoryApplication.class, args);
	}
}


@Data
@Getter
@AllArgsConstructor
class Reservation {

	@Id
	private Integer id;
	private String name;
}


@Configuration
@EnableR2dbcRepositories
class R2dbcConfig extends AbstractR2dbcConfiguration {

	@Override
	public ConnectionFactory connectionFactory() {
		return new PostgresqlConnectionFactory(
			PostgresqlConnectionConfiguration
				.builder()
				.host("localhost")
				.password("0rd3rs")
				.username("orders")
				.database("orders")
				.build()
		);
	}
}

@Log4j2
@Component
class Initializer {

	private final ReservationRepository repository;

	Initializer(ReservationRepository repository) {
		this.repository = repository;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void go() {

		Flux<Customer> customers = Flux
			.just("A", "B", "C", "C")
			.map(name -> new Reservation(null, name))
			.flatMap(this.repository::save);

		repository
			.deleteAll()
			.thenMany(customers)
			.thenMany(this.repository.findByName("C"))
			.subscribe(log::info);
	}
}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, String> {
	Flux<Reservation> findByName(String name);
}