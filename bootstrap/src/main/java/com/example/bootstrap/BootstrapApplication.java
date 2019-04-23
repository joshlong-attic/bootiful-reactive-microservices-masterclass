package com.example.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class BootstrapApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootstrapApplication.class, args);
	}
}

@Component
class CustomHealthIndicator implements HealthIndicator {

	@Override
	public Health health() {
		return Health.status("I <3 Production!").build();
	}
}

@RestController
class GreetingsRestController {

	@GetMapping("/hello/{name}")
	String greet(@PathVariable String name) {
		return String.format("Hello, %s!", name);
	}
}
