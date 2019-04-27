package com.example.reactor.operations;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Log4j2
@SpringBootApplication
public class OperationsApplication {

	public static void main(String args[]) {
		SpringApplication.run(OperationsApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void go() throws InterruptedException {

		var f1 = Flux.just("A", "B", "C", "D");
		var f2 = Flux.just("E", "F");

		log.info("--------------------------------------");
		Flux
			.combineLatest(f1, f2, (a, b) -> a + ':' + b)
			.log()
			.subscribe(log::info);

		log.info("--------------------------------------");
		Flux
			.zip(f1, f2)
			.map(tuple -> tuple.getT1() + ':' + tuple.getT2())
			.subscribe(log::info);

		log.info("--------------------------------------");
		Flux<String> concat = Flux.concat(f1, f2);
		concat.subscribe(log::info);

		log.info("--------------------------------------");
		log.info("time: " + Instant.now());
		Flux<String> defer = Flux
			.defer(() -> {
				var when = Instant.now();
				return f1.map(s -> s + " @ " + when);
			});

		Thread.sleep(2 * 1000);
		log.info("time: " + Instant.now());
		defer.subscribe(log::info);

	}
}

