package com.example.bootcamp;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.function.Function;


@SpringBootApplication
public class BootcampApplication {


	public static void main(String[] args) {
		SpringApplication.run(BootcampApplication.class, args);
	}
}


@Configuration
class WebConfig {

	@Bean
	MyInitializer myInitializer() {
		return new MyInitializer();
	}

	@Bean
	GreetingsService greetingsService() {
		return new GreetingsService();
	}


}

@Log4j2
//@Component
class MyInitializer {

	@EventListener
	public void onReady(ApplicationReadyEvent event) {
		log.info(getClass().getName() + '#' + "afterPropertiesSet");
	}
}

//@Service
class GreetingsService {

	String greet(String name) {
		return "Hello " + name + "@" + Instant.now() + "!";
	}
}

@Log4j2
@RestController
class GreetingsRestController implements InitializingBean {

	private final GreetingsService greetingsService;

	GreetingsRestController(GreetingsService greetingsService) {
		log.info("initializing " + this.getClass().getName());
		this.greetingsService = greetingsService;
	}

	@GetMapping("/hello/{name}")
	String greet(@PathVariable String name) {
		return this.greetingsService.greet(name);
	}

	@PostConstruct
	public void begin() {
		log.info("begin()");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("afterPropertiesSet()");
	}
}

