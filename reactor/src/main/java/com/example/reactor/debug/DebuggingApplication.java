package com.example.reactor.debug;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
public class DebuggingApplication {

	public static void main(String args[]) {
		SpringApplication.run(DebuggingApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void go() {

		Schedulers.enableMetrics();

		var pipeline = Flux.just("A", "B", "C", "D", "E");
		pipeline
			.metrics()
			.log()
			.subscribe();
	}
}