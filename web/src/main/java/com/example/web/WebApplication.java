package com.example.web;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class WebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes() {
		return RouterFunctions
			.route(RequestPredicates.GET("/greetings/{name}").and(request -> Math.random() > .5), this::handleGetGreetings);
	}


	private Mono<ServerResponse> handleGetGreetings(ServerRequest request) {
		return ok().syncBody(new Greeting(request.pathVariable("name")));
	}
}


@RestController
@RequiredArgsConstructor
class GreetingsSseRestController {

	private final GreetingsService greetingsService;

	@GetMapping(value = "/greetings/sse/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<Greeting> greetingFlux(@PathVariable String name) {
		return this.greetingsService.greet(name);
	}
}


@Configuration
class WebsocketConfig {

	@Bean
	WebSocketHandler webSocketHandler(GreetingsService gs) {
		return new WebSocketHandler() {

			@Override
			public Mono<Void> handle(WebSocketSession session) {

				var map = session
					.receive()
					.map(WebSocketMessage::getPayloadAsText)
					.flatMap(gs::greet)
					.map(Greeting::getMessage)
					.map(session::textMessage);

				return session.send(map);
			}
		};
	}


	@Bean
	SimpleUrlHandlerMapping simpleUrlHandlerMapping(WebSocketHandler wsh) {
		return new SimpleUrlHandlerMapping() {
			{
				setUrlMap(Map.of("/ws/greetings", wsh));
				setOrder(10);
			}
		};
	}

	@Bean
	WebSocketHandlerAdapter webSocketHandlerAdapter() {
		return new WebSocketHandlerAdapter();
	}
}


@Service
class GreetingsService {

	Flux<Greeting> greet(String name) {
		return Flux
			.fromStream(Stream.generate(() -> new Greeting("Hello " + name + " @ " + Instant.now()))).delayElements(Duration.ofSeconds(1));
	}
}

/*
@RestController
class GreetingsRestController {

	@GetMapping("/greeting/{name}")
	Mono<Greeting> greeting(@PathVariable String name) {
		// ...
		return Mono.just(new Greeting("Hello " + name + "@" + Instant.now() + "!"));
		// ...
	}
}
*/

@Data
class Greeting {

	Greeting(String name) {
		this.message = "hello " + name + " @ " + Instant.now().toString();
	}

	private String message;
}


