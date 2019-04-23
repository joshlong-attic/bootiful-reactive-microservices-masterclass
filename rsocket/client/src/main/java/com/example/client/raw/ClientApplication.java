package com.example.client.raw;

import com.example.client.GreetingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes(GreetingClient client) {
		return route()
			.GET("/greetings/{name}", serverRequest -> ok()
				.contentType(MediaType.TEXT_EVENT_STREAM)
				.body(client .greet(serverRequest.pathVariable("name")), GreetingResponse.class))
			.build();
	}

}

