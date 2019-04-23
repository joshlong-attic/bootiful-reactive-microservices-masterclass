package com.example.data.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;

@SpringBootApplication
public class MongoApplication {

	public static void main(String args[]) throws IOException {
		SpringApplication.run(MongoApplication.class, args);
		System.in.read();
	}
}

@Log4j2
@Component
class Initializer {

	private final CustomerRepository repository;

	Initializer(CustomerRepository repository) {
		this.repository = repository;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void go() {

		var customers = Flux
			.just("A", "B", "C", "C")
			.map(name -> new Customer(null, name))
			.flatMap(this.repository::save);

		repository
			.deleteAll()
			.thenMany(customers)
			.thenMany(this.repository.findByName("C"))
			.subscribe(log::info);
	}
}

interface CustomerRepository extends ReactiveCrudRepository<Customer, String> {
	Flux<Customer> findByName(String name);
}

@AllArgsConstructor
@Data
@Getter
class Customer {
	private String id, name;
}