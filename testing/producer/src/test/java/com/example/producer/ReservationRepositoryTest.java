package com.example.producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@DataMongoTest
public class ReservationRepositoryTest {

	@Autowired
	private ReservationRepository repository;

	@Test
	public void query() throws Exception {

		Flux<Reservation> results = this.repository
			.deleteAll()
			.thenMany(Flux.just("A", "B").map(n -> new Reservation(null, n)).flatMap(r -> this.repository.save(r)))
			.thenMany(this.repository.findByName("B"));

		StepVerifier
			.create(results)
			.expectNextCount(1)
			.verifyComplete();


	}
}
