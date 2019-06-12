package com.example.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@SpringBootApplication
public class ApiApplication {


	ApiApplication(@Value("${server.port}") int port) {
		this.port = port;
	}

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	private final int port;

	@GetMapping("/greetings/{name}")
	Greeting greeting(@PathVariable String name) {
		String msg = "Hello " + name + "!";
		log.info("greeting: " + msg + " on port " + this.port);
		return new Greeting(msg);
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
	private String name;
}
