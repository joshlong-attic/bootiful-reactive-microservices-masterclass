package com.example.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Instant;

@Log4j2
@RequiredArgsConstructor
@SpringBootApplication
public class DataApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(DataApplication.class, args);
		System.in.read();
	}

	private final ReservationRepository repository;
	private final ReactiveMongoTemplate template;

	@EventListener(ApplicationReadyEvent.class)
	public void basics() {
		Flux<Reservation> saved = Flux
			.just("1", "2", "3", "4")
			.map(name -> new Reservation(null, name))
			.flatMap(this.repository::save);

		this.repository
			.deleteAll()
			.thenMany(saved)
			.subscribe(log::info);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void tailable() {
		this.template
			.dropCollection(Reservation.class)
			.thenMany(this.template.createCollection(Reservation.class, CollectionOptions.empty().capped().size(10)))
			.thenMany(this.repository.findByName("C")).subscribe(r -> log.info("found " + r + " @ " + Instant.now()));
	}

	//	@EventListener(ApplicationReadyEvent.class)
	public void template() {


/*
		Flux<Reservation> saved = Flux
			.just("A", "B", "C", "C")
			.map(name -> new Reservation(null, name))
			.flatMap(this.repository::save);

		this.repository
			.deleteAll()
			.thenMany(saved)
			.thenMany(this.repository.findByName("C"))
			.subscribe(log::info);
*/


		this.template
			.inTransaction()
			.execute(rxTemplate -> rxTemplate
				.dropCollection(Reservation.class)
				.thenMany(rxTemplate.save(new Reservation(null, "A")))
				.thenMany(rxTemplate.save(new Reservation(null, "B")))
				.thenMany(rxTemplate.save(new Reservation(null, "X")))
				.thenMany(rxTemplate.findAll(Reservation.class))
				.doOnNext(reservation -> Assert.isTrue(!reservation.getName().equalsIgnoreCase("X"), "should be X")))
			.subscribe(log::info);

	}
}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, String> {

	@Tailable
	Flux<Reservation> findByName(String name);
}

@Document
@AllArgsConstructor
@Data
class Reservation {

	@Id
	private String id;

	private String name;

}
