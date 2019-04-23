package com.example.producer.spring;

import com.example.GreetingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringRsocketApplication {

	@Bean
	GreetingService greetingService() {
		return new GreetingService();
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringRsocketApplication.class, args);
	}
}

