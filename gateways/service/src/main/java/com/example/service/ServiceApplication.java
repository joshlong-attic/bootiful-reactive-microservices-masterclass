package com.example.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class ServiceApplication {

	@Bean
	RouterFunction<ServerResponse> routes() {
		return route()
			.GET("/greetings/{n}", r -> ok().syncBody(new GreetingResponse("Hello " + r.pathVariable("n") + "!")))
			.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

}


@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingResponse {
	private String message;
}