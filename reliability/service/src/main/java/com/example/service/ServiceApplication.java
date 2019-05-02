package com.example.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Optional;

@SpringBootApplication
public class ServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

}

@Log4j2
@RestController
class ErrorProneRestController {

	@GetMapping("/slow-greet")
	Flux<GreetingsResponse> greetingsResponseFlux(@RequestParam String name) {
		long count = (long) (Math.random() * 30);
		String message = "hello " + name + " (after " + count + " seconds)!";
		log.info("returning: " + message);
		return Flux
			.just(new GreetingsResponse(message))
			.delayElements(Duration.ofSeconds(count));
	}

	@ExceptionHandler(NameNotFoundException.class)
	ResponseEntity<?> errorHandler(NameNotFoundException nnfe) {
		log.info("name not found! " + nnfe.toString());
		return ResponseEntity.badRequest().build();
	}

	@GetMapping("/greet")
	ResponseEntity<GreetingsResponse> greet(@RequestParam Optional<String> name) throws NameNotFoundException {
		return name
			.map(str -> ResponseEntity.ok(new GreetingsResponse("Hello " + str + "!")))
			.orElseThrow(NameNotFoundException::new);
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingsResponse {
	private String message;
}

class NameNotFoundException extends Exception {
}