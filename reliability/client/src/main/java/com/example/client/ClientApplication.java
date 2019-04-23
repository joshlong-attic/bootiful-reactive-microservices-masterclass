package com.example.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	WebClient webClient(WebClient.Builder builder) {
		return builder.build();
	}
}

@RestController
@RequiredArgsConstructor
class ClientRestController {

	private final ErrorProneClient errorProneClient;
	private final ReactiveCircuitBreakerFactory circuitBreakerFactory;

	@GetMapping("/greet")
	Publisher<GreetingsResponse> greet(@RequestParam String name) {
		return errorProneClient.greet(Optional.of(name));
	}

	@GetMapping("/slow-greet")
	Publisher<GreetingsResponse> slowGreet(@RequestParam String name) {
		return errorProneClient.slowGreet(Optional.of(name));
	}

	@GetMapping("/greet-no-param")
	Publisher<GreetingsResponse> greetNoParam() {
		return errorProneClient
			.greet(Optional.ofNullable(null))
			.onErrorResume(throwable -> Flux.just(new GreetingsResponse("Hello world!")));
	}

	@GetMapping("/greet-circuit")
	Publisher<GreetingsResponse> greetWithCircuitBreaker(@RequestParam Optional<String> name) {
		var reply = this.errorProneClient.greet(name);
		return this.circuitBreakerFactory
			.create("greet")
			.run(reply, throwable -> Flux.error(new SimpleException(throwable)));
	}

	// 3
	@GetMapping("/propagate-error")
	Publisher<?> propagateError() {
		return this.errorProneClient
			.greet(Optional.ofNullable(null))
			.onErrorMap(Throwable.class, SimpleException::new);
	}
}

// 3
class SimpleException extends RuntimeException {

	SimpleException(Throwable t) {
		super("OH NO!", t);
	}
}

@Log4j2
@ControllerAdvice
class ErrorHandler {

	// 3
	@ExceptionHandler(SimpleException.class)
	ResponseEntity<?> error(SimpleException se) {
		log.error("oops!");
		return ResponseEntity.badRequest().body(se.getMessage());
	}
}

@Log4j2
@Component
@RequiredArgsConstructor
class ErrorProneClient {

	private final WebClient webClient;
	private final DiscoveryClient discoveryClient;

	Flux<GreetingsResponse> greet(Optional<String> name) {
		return this.doGreet("localhost", 8888, name);
	}

	Flux<GreetingsResponse> slowGreet(Optional<String> name) {
		var discoveryClientInstances = this.discoveryClient.getInstances("service");
		var serviceInstancesRange = discoveryClientInstances.subList(0, 3);
		var listOfPublishers = serviceInstancesRange
			.stream()
			.map(serviceInstance -> doSlowGreeting(serviceInstance.getHost(), serviceInstance.getPort(), name.get()))
			.collect(Collectors.toList());
		return Flux.first(listOfPublishers);
	}

	private Flux<GreetingsResponse> doSlowGreeting(String host, int port, String name) {
		log.info("calling " + host + ':' + port + "...");
		return this.webClient
			.get()
			.uri("http://" + host + ":" + port + "/slow-greet?name=" + name)
			.retrieve()
			.bodyToFlux(GreetingsResponse.class);
	}

	private Flux<GreetingsResponse> doGreet(String host, int port, Optional<String> name) {
		var url = name.map(x -> "?name=" + x).orElse("");
		return this
			.webClient
			.get()
			.uri("http://" + host + ":" + port + "/greet" + url)
			.retrieve()
			.bodyToFlux(GreetingsResponse.class);
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingsResponse {
	private String message;
}