package com.example.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Log4j2
@SpringBootApplication
public class WebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes(GreetingsService greetingsService) {
		String prefix = "/fn";
		return route()
			.GET(prefix + Mappings.GREET, r -> ok().body(greetingsService.greet(r.pathVariable("name")), Greeting.class))
			.GET(prefix + Mappings.GREET_OVER_TIME, r -> ok()
				.contentType(MediaType.TEXT_EVENT_STREAM)
				.body(greetingsService.greetOverTime(r.pathVariable("name")), Greeting.class)
			)
			.filter((serverRequest, handlerFunction) -> {
				log.info("start...");
				try {
					return handlerFunction.handle(serverRequest);
				}
				finally {
					log.info("stop...");
				}
			})
			.build();
	}
}

@Configuration
class WebsocketConfig {

	private final ObjectMapper om;

	WebsocketConfig(ObjectMapper om) {
		this.om = om;
	}

	@SneakyThrows
	String from(Object o) {
		return om.writeValueAsString(o);
	}

	@Bean
	WebSocketHandler webSocketHandler(GreetingsService greetingsService) {
		return webSocketSession -> {
			Flux<WebSocketMessage> world = Flux
				.from(greetingsService.greetOverTime("World"))
				.map(g -> webSocketSession.textMessage(from(g)));
			return webSocketSession.send(world);
		};
	}

	@Bean
	WebSocketHandlerAdapter webSocketHandlerAdapter() {
		return new WebSocketHandlerAdapter();
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
}


interface Mappings {
	String GREET = "/greet/{name}";
	String GREET_OVER_TIME = "/greet-timed/{name}";
}

@Service
class GreetingsService {

	Publisher<Greeting> greet(String name) {
		return Flux.just(new Greeting("hello " + name + "!"));
	}

	Publisher<Greeting> greetOverTime(String name) {
		return Flux
			.fromStream(Stream.generate(() -> new Greeting("Hello " + name + " @ " + Instant.now())))
			.delayElements(Duration.ofSeconds(1));
	}
}

@RequestMapping("/rc")
@RestController
class GreetingsRestController {

	private final GreetingsService greetingsService;

	GreetingsRestController(GreetingsService greetingsService) {
		this.greetingsService = greetingsService;
	}

	@GetMapping(value = Mappings.GREET_OVER_TIME, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Publisher<Greeting> greetOverTime(@PathVariable String name) {
		return this.greetingsService.greetOverTime(name);
	}

	@GetMapping(Mappings.GREET)
	Publisher<Greeting> greet(@PathVariable String name) {
		return this.greetingsService.greet(name);
	}
}

@Data
@AllArgsConstructor
class Greeting {
	private String message;
}