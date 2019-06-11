package com.example.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Log4j2
@SpringBootApplication
@EnableTransactionManagement
@RequiredArgsConstructor
public class MongoApplication {

	private final ReservationRepository reservationRepository;
	private final ReactiveMongoTemplate template;

	public static void main(String[] args) throws IOException {
		SpringApplication.run(MongoApplication.class, args);
		System.in.read();
	}

	@Bean
	TransactionalOperator transactionalOperator(ReactiveTransactionManager txm) {
		return TransactionalOperator.create(txm);
	}


	@Bean
	ReactiveTransactionManager transactionManager(ReactiveMongoDatabaseFactory cf) {
		return new ReactiveMongoTransactionManager(cf);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initializeCollection() {

		this.template
			.dropCollection(Reservation.class)
			.thenMany(this.template.createCollection(Reservation.class))
			.subscribe();

//			.dropCollection(Reservation.class)
//			.thenMany(this.template.createCollection(Reservation.class, CollectionOptions.empty().capped().size(10)))

		/* // demos tailable queries
		this.reservationRepository.findByName("Arjen") .subscribe(log::info); */
	}

}


@Component
@RequiredArgsConstructor
class TransactionalDemo {

	private final ReservationRepository reservationRepository;
	private final ReservationService service;

	@EventListener(ApplicationReadyEvent.class)
	public void writeNames() {

		this.reservationRepository
			.deleteAll()
			.thenMany(this.service.save("Alfred@a.com", "Bob@b.com", "Carter@c.com", "Daniel@d.com"))
			.subscribe();
	}

}


@Service
@RequiredArgsConstructor
class ReservationService {

	private final ReservationRepository reservationRepository;
	private final TransactionalOperator transactionalOperator;

	//	@Transactional
	public Flux<Reservation> save(String... names) {

		Flux<Reservation> reservations = Flux
			.just(names)
			.map(name -> new Reservation(null, name))
			.flatMap(this.reservationRepository::save)
			.doOnNext(r -> validateName(r.getName()));

		return transactionalOperator.transactional(reservations);
	}

	private static void validateEmail(String email) {
		Assert.isTrue(email != null && email.length() > 0 && email.contains("@"), "the email is invalid!");
	}

	private static void validateName(String name) {
		Assert.isTrue(name != null, "the name can't be null");
		Assert.isTrue(name.length() > 0, "the name can't be null");
		var firstChar = name.charAt(0);
		Assert.isTrue(Character.isUpperCase(firstChar), "the first char must be uppercase!");
	}

}


@Log4j2
//@Component
@RequiredArgsConstructor
class ReservationInitializer {

	private final ReservationRepository reservationRepository;

	@EventListener(ApplicationReadyEvent.class)
	public void go() {

		var names = Flux
			.just("Josh", "Madhura", "Dave", "Cornelia", "Stephane", "Olga", "Arjen", "Violetta")
			.map(nom -> new Reservation(null, nom))
			.flatMap(this.reservationRepository::save);

		this.reservationRepository
			.deleteAll()
			.thenMany(names)
			.thenMany(this.reservationRepository.findByName("Arjen"))
			.subscribe(log::info);
	}
}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, String> {

	@Tailable
	Flux<Reservation> findByName(String name);
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
class Reservation {

	@Id
	private String id;

	private String name;
}